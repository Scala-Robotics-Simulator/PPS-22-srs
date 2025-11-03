find ./obstacle-avoidance -type f -name "*.yml" -exec sed -i \
  -e 's/reward: .*/reward: NewRewardValue/' \
  -e 's/termination: .*/termination: NewTerminationValue/' \
  -e 's/truncation: .*/truncation: NewTruncationValue/' \
  {} +
