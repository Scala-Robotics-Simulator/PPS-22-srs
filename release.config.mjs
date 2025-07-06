import config from "semantic-release-preconfigured-conventional-commits" with { type: "json" };

const publishCmd = `
echo "HAS_RELEASED=true" >> $GITHUB_ENV
echo "RELEASE_VERSION="\${nextRelease.version} >> $GITHUB_ENV
git tag -a -f v\${nextRelease.version} v\${nextRelease.version} -F CHANGELOG.md  || exit 1
export CI_COMMIT_TAG="true"
`;

const releaseBranches = ["main"];
config.branches = releaseBranches;

config.plugins.push(
  [
    "@semantic-release/exec",
    {
      publishCmd: publishCmd,
    },
  ],
  [
    "@semantic-release/github",
    {
      assets: [{ path: "target/fatjar/*.jar" }],
    },
  ],
  "@semantic-release/git",
);
config.tagFormat = "v${version}";

export default config;
