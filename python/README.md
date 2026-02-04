# SRS Reinforcement Learning library

A reinforcement learning library written in python for the SRS project.

## Requirements

The library is managed with `uv`, so you need to install it first. You can do so with:

- if you are on linux or macOS, you can use the following command:

    ```bash
    curl -LsSf https://astral.sh/uv/install.sh | sh
    ```

    If you don't have `curl`, you can use `wget`:

    ```bash
    wget -qO- https://astral.sh/uv/install.sh | sh
    ```

- if you are on Windows, you can use the following command in PowerShell:

    ```powershell
    powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
    ```

If you have any issues, please refer to the [official installation guide](https://docs.astral.sh/uv/getting-started/installation/).

Once you have `uv` installed, you can create a virtual environment and install the dependencies with:

```bash
uv venv
```

And follow the instructions to activate the virtual environment.
Then, you can install the dependencies with:

```bash
uv pip install -r pyproject.toml
```

## Documentation

The introduction of *autonomous agents* in the system has been done for the **Reinforcement Learning** course.
The main documentation of the project can be found at [report.ipynb](./src/notebooks/report/index.ipynb), while task specific information is located at:

- [exploration](./src/notebooks/report/exploration.ipynb)
- [obstacle avoidance](./src/notebooks/report/obstacle-avoidance.ipynb)
- [phototaxis](./src/notebooks/report/phototaxis.ipynb)

## Training and evaluation

### Environment generation

The script [generate_environments.py](./src/scripts/generate_environments.py) can be used to generate ad-hoc environments for the task at hand. Documentation for the script is located inside the file itself.

### Training

To train either a Q-Agent or Deep Q-Agent the following scripts can be used [train_qagent.py](./src/scripts/train-qagent.py) and [train_dqagent.py](./src/scripts/train-dqagent.py).
Creating new training scenarios is as easy as creating a custom environment file, just like [exploration_env.py](./src/environment/qlearning/exploration_env.py) and new [reward](../src/main/scala/io/github/srs/model/entity/dynamicentity/agent/reward/Reward.scala), [termination](../src/main/scala/io/github/srs/model/entity/dynamicentity/agent/termination/Termination.scala) and [truncation](../src/main/scala/io/github/srs/model/entity/dynamicentity/agent/truncation/Truncation.scala) functions. Or reuse the existing ones.

### Validation

Validation can be done either visually, as shown in [show_training_results.ipynb](./src/notebooks/q-learning/show_training_results.ipynb) or via a more thorough analysis of success rate, moving average reward, temporal difference loss and steps to success as shown in the task specific notebooks above.

