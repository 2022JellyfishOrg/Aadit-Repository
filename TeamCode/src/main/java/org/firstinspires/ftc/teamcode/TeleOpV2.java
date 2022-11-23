package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
public class TeleOpV2 extends LinearOpMode {
    double wheelDiameter = 3.77953;
    double circumference = wheelDiameter * Math.PI * 0.513;
    double circumferenceLift = 2 * Math.PI;
    double ticksPerRevolution = 537.6;
    int liftTicks = (int) (250 / circumferenceLift);  // variable value because of inconsistencies
    double turnDenom = 0.8;
    double denominator = 0.5;


    int signalZonePos = 0;
    boolean lastA = false;
    boolean direction = true;
    double lowLift = 14.2;
    double mediumLift = 23;
    double highLift = 32.5;
    DcMotor backLeft;
    DcMotor backRight;
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor lift;

    Servo claw;

    @Override
    public void runOpMode() throws InterruptedException {


        frontLeft = hardwareMap.get(DcMotor.class, "fl");
        frontRight = hardwareMap.get(DcMotor.class, "fr");
        backLeft = hardwareMap.get(DcMotor.class, "bl");
        backRight = hardwareMap.get(DcMotor.class, "br");

        lift = hardwareMap.get(DcMotor.class, "lift");
        claw = hardwareMap.get(Servo.class, "claw");

        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.REVERSE);


        waitForStart();

        while (opModeIsActive()) {
            if (lift.getCurrentPosition() == 0) {
                lift.setPower(0);
            }
            if (gamepad2.left_stick_y < 0 && lift.getCurrentPosition() <= liftTicks * highLift + 50) {
                if (lift.getPower() == 0) {
                    lift.setPower(0.5);
                }
                if (lift.getCurrentPosition() <= liftTicks * highLift + 100) {
                    lift.setTargetPosition(lift.getCurrentPosition() + 60);
                    lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                }
            } else if (gamepad2.left_stick_y > 0 && lift.getCurrentPosition() >= -50) {
                if (lift.getPower() == 0) {
                    lift.setPower(0.5);
                }
                if (lift.getCurrentPosition() >= 0) {
                    lift.setTargetPosition(lift.getCurrentPosition() - 60);
                    lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                }


            }

            if (lift.getCurrentPosition() < liftTicks * lowLift) {
                turnDenom = 1;
            } else if (lift.getCurrentPosition() < liftTicks * mediumLift) {
                turnDenom = 0.3;
            } else if (lift.getCurrentPosition() < liftTicks * highLift) {
                turnDenom = 0.3;
            } else {
                turnDenom = 0.2;
            }
            double y = gamepad1.left_stick_y * denominator;// Remember, this is reversed!
            double x = -gamepad1.left_stick_x * 1.1 * denominator; // Counteract imperfect strafing
            double rx = -gamepad1.right_stick_x * turnDenom; // turning

            double frontLeftPower = (y + x + rx);
            double backLeftPower = (y - x + rx);
            double frontRightPower = (y - x - rx);
            double backRightPower = (y + x - rx);


            frontLeft.setPower(frontLeftPower);
            backLeft.setPower(backLeftPower);
            frontRight.setPower(frontRightPower);
            backRight.setPower(backRightPower);

            // A, A
            /*
            if (gamepad1.b) {
                denominator = 1;
            } else {
                denominator = 0.5;
            }

             */

            // manual lift (test)

            if ((gamepad1.a && !lastA)) {
                direction = !direction;
                double res = 1;
                if (!direction) {
                    res = 0.53;
                } else {
                    res = 1;
                }
                claw.setPosition(res);
            }
            lastA = gamepad1.a;


            /*
            if (gamepad1.a) {
                claw.setPosition(1);
            }
            if (gamepad1.b) {
                claw.setPosition(0.55);
            }

             */




            // lift macros
            if (gamepad2.dpad_down) {
                double speed = 1;
                if (lift.getCurrentPosition() > liftTicks * lowLift) {
                    speed = 0.6;
                }
                // ticks = (ticks per inch)(# of inches)
                //claw.setPosition(1);

                int ticks = (int) (liftTicks * lowLift);

                lift.setPower(speed);
                lift.setTargetPosition(ticks);


                lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                Thread.sleep(250);
                //claw.setPosition(1);
                denominator = 0.35;
            } else if (gamepad2.dpad_left) {
                double speed = 1;
                if (lift.getCurrentPosition() > liftTicks * mediumLift) {
                    speed = 0.6;
                }
                //claw.setPosition(1);
                // ticks = (ticks per inch)(# of inches)
                int ticks = (int) (liftTicks * mediumLift);
                lift.setPower(speed);
                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

                lift.setTargetPosition(ticks);

                //claw.setPosition(1);
                lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                Thread.sleep(250);
                    /* while (lift.isBusy()) {

                    } */
                //claw.setPosition(1);

                denominator = 0.35;
            } else if (gamepad2.dpad_up) {
                double speed = 1;
                if (lift.getCurrentPosition() > liftTicks * highLift) {
                    speed = 0.6;
                }
                //claw.setPosition(1);
                // ticks = (ticks per inch)(# of inches)
                int ticks = (int) (liftTicks * highLift);

                lift.setPower(speed);
                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift.setTargetPosition(ticks);

                lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                Thread.sleep(250);
                     /* while (lift.isBusy()) {

                    } */
                //claw.setPosition(1);

                denominator = 0.25;
                // opening the claw

                // resetting claw to closed
            } else if (gamepad2.dpad_right) { // resetting lift
                // ticks = (ticks per inch)(# of inches)
                claw.setPosition(0.53);
                int ticks = 0;
                lift.setPower(0.8);

                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift.setTargetPosition(ticks);

                lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                Thread.sleep(250);
                    /* while (lift.isBusy()) {

                    } */

                denominator = 0.5;
                // opening the claw

            } else if (gamepad2.a) {
                // ticks = (ticks per inch)(# of inches)
                int ticks = 0;
                lift.setPower(0.8);

                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lift.setTargetPosition(ticks);

                lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                Thread.sleep(250);
                    /* while (lift.isBusy()) {

                    } */

                denominator = 0.5;
                // opening the claw
            }


        }
    }
}