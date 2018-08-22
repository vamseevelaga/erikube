#!/bin/bash
set -ex
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd -P )

tmp_dir=./tmp-store
rm -rf $tmp_dir
mkdir -p $tmp_dir
"$DIR/download-erikube-images.sh" $tmp_dir/
if [[ "$(ls $tmp_dir/)"x == x ]];then
  echo "There seems to be no image downloaded"
  exit 1
fi

if [[ $GIT_BRANCH == origin/rel/?.?.? ]]; then  # Get GIT_BRANCH, WORKSPACE from Jenkins ENV variables.
    cd "$tmp_dir"
    tarball_end="-x86_64.tgz"
    version=$(ls *$tarball_end |cut -d "-" -f2)
    location=$(ls *${tarball_end} | sed -e "s/${tarball_end}$//" |sed -e "s/eccd-//")
    cd "${WORKSPACE}"
    "$DIR/upload-artifact.sh" "rc/$version/$location" "$tmp_dir/"  # Upload RC to new location on artifactory
else
    "$DIR/delete_artifact.sh" latest/stable/ ||true
    "$DIR/upload-artifact.sh" latest/stable/ $tmp_dir/
fi

rm -rf $tmp_dir
