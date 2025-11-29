find ./generated/phototaxis/envs1 -type f -name "*.yml" -exec sed -i \
  -e 's/reward: .*/reward: Phototaxis/' \
  -e 's/termination: .*/termination: CrashOrReached/' \
  -e 's/truncation: .*/truncation: NeverTruncate/' \
  {} +
