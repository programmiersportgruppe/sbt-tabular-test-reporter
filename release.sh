#!/usr/bin/env bash

set -e
set -u
set -o pipefail


if [[ -z "${1-}" ]]; then
    echo "Missing parameter version">&2
    exit 1
fi

version="$1"

dirty=$(git status --porcelain)
if [ "${dirty}" ]; then
    echo "Cannot release because these files are dirty:"
    echo "${dirty}"
    exit 1
fi >&2

git pull --rebase

find . -name target -prune -exec rm -r {} \;

sbt "set version in ThisBuild := \"${version}\"" +test +publishSigned

tag="v${version}"

previous_release_tag=$( git describe --abbrev=0 || git rev-list HEAD | tail -n 1 )


git log ${previous_release_tag}..HEAD > /tmp/release-notes-candidate.txt

vim /tmp/release-notes-candidate.txt

release_notes=$(cat /tmp/release-notes-candidate.txt)

git branch --force "release-${tag}"
git checkout "release-${tag}"

sed -i "" -E 's/(.*"org.programmiersportgruppe.sbt" %% "[^"]*" % ")[^"]*(.*)/\1'"${version}"'\2/' README.md
git commit -a -m "Releasing ${version}." || echo "Nothing to commit"

git tag -f "${tag}" --annotate --message "${release_notes}"

git push origin "${tag}" "${tag}:master"

git checkout master
git pull --rebase

git branch -d "release-${tag}"
