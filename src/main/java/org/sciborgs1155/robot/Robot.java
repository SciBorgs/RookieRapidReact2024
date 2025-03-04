package org.sciborgs1155.robot;

import static edu.wpi.first.wpilibj2.command.button.RobotModeTriggers.*;
import static edu.wpi.first.wpilibj2.command.button.RobotModeTriggers.autonomous;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ProxyCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import monologue.Annotations.Log;
import monologue.Logged;
import monologue.Monologue;
import org.sciborgs1155.lib.CommandRobot;
import org.sciborgs1155.lib.FaultLogger;
import org.sciborgs1155.robot.Drive.Drive;
import org.sciborgs1155.robot.Hopper.*;
import org.sciborgs1155.robot.Intake.*;
import org.sciborgs1155.robot.Ports.OI;
import org.sciborgs1155.robot.commands.Autos;
import org.sciborgs1155.robot.shooter.Shooter;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class Robot extends CommandRobot implements Logged {

  // INPUT DEVICES
  private final CommandXboxController operator = new CommandXboxController(OI.OPERATOR);
  private final CommandXboxController driver = new CommandXboxController(OI.DRIVER);

  // SUBSYSTEMS
  @Log.File private final Drive drive = new Drive();
  @Log.File private final Shooter shooter = new Shooter();
  @Log.File private final Intake intake = new Intake();
  @Log.File private final Hopper hopper = new Hopper();

  // COMMANDS
  @Log.NT Autos autos = new Autos();

  /** The robot contains subsystems, OI devices, and commands. */
  public Robot() {
    configureGameBehavior();
    configureSubsystemDefaults();
    configureBindings();
  }

  /** Configures basic behavior during different parts of the game. */
  private void configureGameBehavior() {
    if (isSimulation()) {
      DriverStation.silenceJoystickConnectionWarning(true);
    }

    // Configure logging with DataLogManager, Monologue, and FailureManagement
    DataLogManager.start();
    Monologue.setupMonologue(this, "/Robot", false, true);
    addPeriodic(Monologue::updateAll, kDefaultPeriod);
    FaultLogger.setupLogging();
    addPeriodic(FaultLogger::update, 1);
  }

  /**
   * Configures subsystem default commands. Default commands are scheduled when no other command is
   * running on a subsystem.
   */
  private void configureSubsystemDefaults() {
    drive.setDefaultCommand(
        drive.setMotorSpeeds(() -> -driver.getLeftY(), () -> -driver.getRightY()));
  }

  /** Configures trigger -> command bindings */
  double
      launchAngle; // find from method written to optimize time of travel for a given distance with

  // variables angle and speed

  private void configureBindings() {
    operator.x().whileTrue(intake.intake().alongWith(hopper.forward()));
    operator
        .a()
        .whileTrue(
            Commands.parallel(
                shooter.shoot(),
                Commands.waitUntil(shooter::isAtShootingSpeed).andThen(hopper.forward())));
    operator.b().whileTrue(intake.outtake());
    autonomous().whileTrue(new ProxyCommand(autos::get));
    FaultLogger.onFailing(f -> Commands.print(f.toString()));
  }

  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }
}
