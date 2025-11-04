find ./generated/phototaxis/conf -type f -name "*.yml" -exec sed -i \
  -e 's/reward: .*/reward: Phototaxis/' \
  -e 's/termination: .*/termination: CrashOrReached/' \
  -e 's/truncation: .*/truncation: NeverTruncate/' \
  -e 's/illuminationRadius: .*/illuminationRadius: 7.0/' \
  {} +
