set -o pipefail
set -e
set -x

source ./scripts/generate-steps.sh

#### Configuration of env variables

# SPRING_GENERATOR_DIR - default to working directory
# LIBRARIES_BOM_VERSION - default to parse from spring cloud gcp BOM, on branch of script execution
# MONOREPO_TAG - default to parse given LIBRARIES_BOM_VERSION
# LIBRARY_LIST_PATH - if not provided, generates list at ${SPRING_GENERATOR_DIR}/scripts/resources/library_list.txt

# If not set, assume working directory is spring-cloud-generator
if [[ -z "$SPRING_GENERATOR_DIR" ]]; then
  echo "No SPRING_GENERATOR_DIR override provided, assuming working directory"
  SPRING_GENERATOR_DIR=`pwd`
fi
SPRING_ROOT_DIR=${SPRING_GENERATOR_DIR}/..

# Reset target folder for generated code
cd ${SPRING_GENERATOR_DIR}
echo "executing reset_previews_folder"
reset_previews_folder

# Compute the Spring Cloud GCP project version.
cd ${SPRING_ROOT_DIR}
PROJECT_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

# If not provided, parse libraries-bom version from spring cloud gcp BOM
if [[ -z "$LIBRARIES_BOM_VERSION" ]]; then
  echo "No LIBRARIES_BOM_VERSION override provided, parsing from spring cloud gcp BOM"
  LIBRARIES_BOM_VERSION=$(xmllint --xpath "string(//*[local-name()='gcp-libraries-bom.version'])" ${SPRING_ROOT_DIR}/spring-cloud-gcp-dependencies/pom.xml)
fi

# If not provided, compute monorepo tag from libraries BOM version
if [[ -z "$MONOREPO_TAG" ]]; then
  echo "No MONOREPO_TAG override provided, computing from LIBRARIES_BOM_VERSION: ${LIBRARIES_BOM_VERSION}"
  MONOREPO_TAG="v$(compute_monorepo_version ${LIBRARIES_BOM_VERSION})"
fi

# If not provided, generate and set library list path variable
if [[ -z "$LIBRARY_LIST_PATH" ]]; then
  echo "No LIBRARY_LIST_PATH override provided, generating for MONOREPO_TAG: ${MONOREPO_TAG}"
  cd ${SPRING_GENERATOR_DIR}
  generate_libraries_list ${MONOREPO_TAG}
  LIBRARY_LIST_PATH=${SPRING_GENERATOR_DIR}/scripts/resources/library_list.txt
fi

# Log environment variables after configuration
echo "Spring root dir: ${SPRING_ROOT_DIR}"
echo "Spring generator dir: ${SPRING_GENERATOR_DIR}"
echo "Project version: ${PROJECT_VERSION}"
echo "Libraries BOM version: ${LIBRARIES_BOM_VERSION}"
echo "Monorepo tag: ${MONOREPO_TAG}"
echo "Library list path: ${LIBRARY_LIST_PATH}"

#### Execute prepare and generation steps

cd ${SPRING_GENERATOR_DIR}

echo "executing setup_googleapis"
setup_googleapis

LIBRARIES=$(cat ${SPRING_GENERATOR_DIR}/scripts/resources/library_list.txt | tail -n+2)

# For each of the entries in the library list, prepare googleapis folder
echo "looping over libraries to prepare bazel build"
while IFS=, read -r library_name googleapis_location coordinates_version googleapis_commitish monorepo_folder; do
  echo "preparing protos and bazel rules for $library_name"
  prepare_bazel_build $googleapis_commitish $googleapis_location 2>&1 | tee tmp-output || save_error_info ${SPRING_GENERATOR_DIR} "bazel_prepare_$library_name"
done <<< "${LIBRARIES}"

# add commit to debug branch in repo. This branch is wiped out each time.
# This debug branch contains googleapis alterations before invoking bazel_build_all
cd ${SPRING_GENERATOR_DIR}
echo "remove googleapis/.git to avoid conflict"
rm -rf googleapis/.git

debug_branch="debug-spring-rules"
remote="my-graal-test"

# record the current branch
current_branch=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch working on: $current_branch"
# Check if the branch exists on the remote
branch_exists=$(git ls-remote --heads $remote $debug_branch)

if [[ -n $branch_exists ]]; then
  # Branch exists - proceed with deletion
  echo "Branch '$debug_branch' found on remote '$remote'."
  git push $remote --delete $debug_branch
  # cleanup local
  git fetch --prune
fi

echo "creating $debug_branch branch, add commit and push to remote"
git checkout -b $debug_branch ||  { echo "Error creating or switching to new branch. Exiting." && exit 1; }
# remove .git so content can be added to commit for debug

git add .
git commit -m "debug: added all changes to repo before fetching all `*java_gapic_spring` build rules and build them."
echo "commit added, now push to remote."
git push $remote $debug_branch

#echo "switch back to original branch again."
#git checkout $current_branch
#
## Invoke all bazel build targets
#echo "invoking bazel_build_all"
#cd ${SPRING_GENERATOR_DIR}/googleapis
#bazel_build_all ||  { echo "Error in bazel_build_all step. Exiting." && exit 1; }
#cd ${SPRING_GENERATOR_DIR}
#
## For each of the entries in the library list, perform post-processing steps
#echo "looping over libraries to perform post-processing"
#while IFS=, read -r library_name googleapis_location coordinates_version googleapis_commitish monorepo_folder; do
#  echo "processing library $library_name"
#  group_id=$(echo $coordinates_version | cut -f1 -d:)
#  artifact_id=$(echo $coordinates_version | cut -f2 -d:)
#  postprocess_library $artifact_id $group_id $PROJECT_VERSION $googleapis_location $monorepo_folder $googleapis_commitish $MONOREPO_TAG 2>&1 | tee tmp-output || save_error_info ${SPRING_GENERATOR_DIR} "postprocess_$library_name"
#done <<< "${LIBRARIES}"
#
## Clean up downloaded repo and output file
#rm tmp-output
#rm -rf googleapis
#
## Format generated code
#echo "running formatter on generated code"
#run_formatter ${SPRING_ROOT_DIR}/spring-cloud-previews
#
