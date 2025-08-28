package io.github.srs.view.components.simulation

import javax.swing.{ BorderFactory, JPanel }

import scala.concurrent.duration.FiniteDuration

import io.github.srs.utils.SimulationDefaults.UI
import io.github.srs.view.components.UIUtils
import io.github.srs.utils.time.TimeUtils.formatTime

/**
 * Panel displaying simulation timing information including elapsed time and remaining time.
 */
class TimePanel extends JPanel:

  import javax.swing.{ JLabel, SwingConstants, SwingUtilities }
  import java.awt.{ Font, GridLayout }

  private val elapsedLabel = new JLabel("Elapsed:")
  private val elapsedValue = new JLabel("00:00:00", SwingConstants.RIGHT)
  private val remainingLabel = new JLabel("Remaining:")
  private val remainingValue = new JLabel("--:--:--", SwingConstants.RIGHT)

  initLayout()

  /**
   * Initializes the panel components with styling and layout.
   */
  private def initLayout(): Unit =
    setLayout(new GridLayout(2, 2, 10, 5))
    setBackground(UI.Colors.backgroundMedium)
    setBorder(
      BorderFactory.createCompoundBorder(
        UIUtils.paddedBorder(),
        UIUtils.titledBorder("Simulation Time"),
      ),
    )

    // Style the labels
    List(elapsedLabel, remainingLabel).foreach { label =>
      label.setFont(label.getFont.deriveFont(Font.BOLD))
    }

    List(elapsedValue, remainingValue).foreach { value =>
      value.setFont(new Font("Monospaced", Font.PLAIN, UI.Fonts.FontSize))
      value.setBackground(UI.Colors.backgroundLight)
    }

    add(elapsedLabel)
    add(elapsedValue)
    add(remainingLabel)
    add(remainingValue): Unit

  end initLayout

  /**
   * Updates the time display with current simulation times.
   *
   * @param elapsed
   *   The elapsed simulation time
   * @param total
   *   Optional total simulation time (for calculating remaining)
   */
  def updateTimes(elapsed: FiniteDuration, total: Option[FiniteDuration]): Unit =
    SwingUtilities.invokeLater { () =>
      elapsedValue.setText(formatTime(elapsed))

      total match
        case Some(maxTime) =>
          val remaining = maxTime - elapsed
          if remaining.toMillis > 0 then remainingValue.setText(formatTime(remaining))
          else remainingValue.setText("00:00:00")
        case None =>
          remainingValue.setText("∞")
    }

  /**
   * Resets the time display to initial values.
   */
  def reset(): Unit =
    SwingUtilities.invokeLater { () =>
      elapsedValue.setText("00:00:00")
      remainingValue.setText("--:--:--")
    }

end TimePanel
