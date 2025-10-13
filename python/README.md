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
