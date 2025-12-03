import numpy as np
from keras.layers import Dense, Input, GRU
from keras.models import Sequential
from keras.optimizers import Adam
from keras.utils import plot_model


class DQNetwork:
    """Deep Q-Network model.

    Parameters
    ----------
    input_count : tuple
        Shape of the input state.
    neuron_count_per_hidden_layer : list of int
        Number of neurons in each hidden layer.
    action_count : int
        Number of possible actions.
    summary : bool, optional (default=False)
        Whether to print the model summary.
    plot_model_flag : bool, optional (default=False)
        Whether to plot the model architecture.
    """

    def __init__(
        self,
        input_count: tuple,
        neuron_count_per_hidden_layer: list,
        action_count: int,
        summary: bool = False,
        plot_model_flag: bool = False,
    ):
        self.input_count = input_count
        self.neuron_count_per_hidden_layer = neuron_count_per_hidden_layer
        self.action_count = action_count
        self.model = self._build_simple_dqn()
        if summary:
            self.model.summary()
        if plot_model_flag:
            plot_model(self.model, show_shapes=True, show_layer_names=False)

    def _build_simple_dqn(self):
        """Builds a simple Deep Q-Network model.

        Returns
        -------
        keras.Model
            The constructed Deep Q-Network model.
        """
        model = Sequential()
        # TODO model.add(Input(shape=self.input_count, name="Input"))

        model.add(Input(shape=(4, self.input_count), name="Input"))

        model.add(GRU(64, return_sequences=False))

        for n in self.neuron_count_per_hidden_layer:
            model.add(Dense(n, activation="relu"))

        model.add(Dense(self.action_count, name="Output"))
        model.compile(loss="mse", optimizer=Adam(learning_rate=0.001))

        return model

    def update_weights(self, target_network: "DQNetwork"):
        """Sets the weights of the current network to those of the target network.

        Parameters
        ----------
        target_network : DQNetwork
            The target network from which to copy weights.
        """
        self.model.set_weights(target_network.model.get_weights())

    def predict(self, state: np.ndarray) -> np.ndarray:
        """Predicts Q-values for the given state.

        Parameters
        ----------
        state : np.ndarray
            The input state for which to predict Q-values.

        Returns
        -------
        np.ndarray
            Predicted Q-values for the input state."""
        return self.model.predict(state)
