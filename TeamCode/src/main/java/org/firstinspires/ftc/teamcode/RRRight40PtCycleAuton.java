package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.compfiles.Detector;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

@Autonomous
public class RRRight40PtCycleAuton extends LinearOpMode {

    // Declaring hardware
    DcMotor frontLeft, frontRight, backLeft, backRight, lift1, lift2;
    Servo claw, arm;

    // For lift method
    int notApplicable = 0;

    // claw open/closed values
    double openClaw = 0.53;
    double closedClaw = 1;

    // arm open/closed values
    int backArm = 125;
    int frontArm = 0;
    int cycles = 3;

    // lift height/speed values
    double liftSpeed = 0.5;
    double lowLift = 15;
    double mediumLift = 23;
    double highLift = 33;
    double circumferenceLift = 2 * Math.PI;
    int liftTicks = (int) (250 / circumferenceLift);

    // counting number of auton cones to measure height
    int countCones = 0;

    // vision position detection
    private static int signalZonePos;

    // Roadrunner coordinates:
    public static int startPoseX = 34;
    public static int startPoseY = -61;
    public static int depositX = 29;
    public static int depositY = -12;
    public static int loadX = 34;
    public static int loadY = -36;
    public static int initParkX = 34;
    public static int parkX = 34 + (signalZonePos - 2) * 12;
    public static int parkY = depositY;

    // Declaring trajectories
    Trajectory toPreload;
    Trajectory toLoad;
    Trajectory toDeposit;
    Trajectory toFinalDeposit;
    Trajectory toAlignment;
    Trajectory toPark;

    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Initializing.");

        // WEBCAM STUFF
        OpenCvWebcam webcam;
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        int cameraMonitorViewId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance()
                .createWebcam(hardwareMap.get(WebcamName.class,"webby"), cameraMonitorViewId);
        Detector detector = new Detector(telemetry);
        webcam.setPipeline(detector);
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }
            @Override
            public void onError(int errorCode) {
            }
        });

        // start position of robot (NEEDS TO BE ACCURATE TO ABOUT AN INCH OR LESS)
        Pose2d startPose = new Pose2d(startPoseX, startPoseY, 0);

        // setting robot "drive" position to the start position above
        drive.setPoseEstimate(startPose);

        toPreload = drive.trajectoryBuilder(startPose)
                .strafeTo(new Vector2d(depositX + 5, depositY))
                .lineTo(new Vector2d(depositX, depositY))
                .addDisplacementMarker(() -> drive.followTrajectoryAsync(toLoad))
                .build();
        toLoad = drive.trajectoryBuilder(toPreload.end())
                .lineTo(new Vector2d(loadX, loadY))
                .addDisplacementMarker(() -> drive.followTrajectoryAsync(toDeposit))
                .build();
        toDeposit = drive.trajectoryBuilder(toLoad.end())
                .lineTo(new Vector2d(depositX, depositY))
                .addDisplacementMarker(() -> drive.followTrajectoryAsync(toLoad))
                .build();
        toFinalDeposit = drive.trajectoryBuilder(toLoad.end())
                .lineTo(new Vector2d(depositX, depositY))
                .addDisplacementMarker(() -> drive.followTrajectoryAsync(toAlignment))
                .build();
        toPark = drive.trajectoryBuilder(toDeposit.end())
                .lineTo(new Vector2d(depositX + 5, depositY))
                .strafeTo(new Vector2d(initParkX, depositY))
                .lineTo(new Vector2d(parkX, parkY))
                .addDisplacementMarker(() -> drive.followTrajectoryAsync(toPark))
                .build();


        frontLeft = hardwareMap.get(DcMotor.class, "fl");
        frontRight = hardwareMap.get(DcMotor.class, "fr");
        backLeft = hardwareMap.get(DcMotor.class, "bl");
        backRight = hardwareMap.get(DcMotor.class, "br");

        lift1 = hardwareMap.get(DcMotor.class, "lift");
        lift2 = hardwareMap.get(DcMotor.class, "lift2");
        claw = hardwareMap.get(Servo.class, "claw");

        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        lift1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        lift2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();


        signalZonePos = detector.getPosition();
        telemetry.addData("zone", signalZonePos);
        telemetry.update();

        // move lift to high (0 is reset, 1 is low, 2 is medium, 3 is high))
        // ifCone represents if lift is picking up auton cone or depositing (true: auton cone, false: depositing)
        liftConfig(3, false);

        // rotate arm to back
        armPresets(backArm);

        // follow path to preload
        drive.followTrajectory(toPreload);

        // CYCLES FOR "cycles" amount of times
        for (int i = 0; i < cycles; i++) {

            // open claw
            Thread.sleep(250);
            claw.setPosition(openClaw);

            // reset arm
            armPresets(frontArm);

            // reset lift to auton cone height
            liftConfig(notApplicable, true);

            // drive to auton cones
            drive.followTrajectory(toLoad);
            whileMotorsActive();

            // grab cone
            Thread.sleep(250);
            claw.setPosition(closedClaw);

            // moving cone up BEFORE following path
            liftConfig(3, false);
            Thread.sleep(250);

            // start rotating arm
            armPresets(backArm);

            // Go back to deposit
            drive.followTrajectory(toDeposit);
            whileMotorsActive();



            // LOOP ENDS
        }

        Thread.sleep(250);
        claw.setPosition(openClaw);

        liftConfig(0, true);

        drive.followTrajectory(toPark);
        claw.setPosition(closedClaw);

    }

    public void whileMotorsActive() {
        while (frontLeft.isBusy() || frontRight.isBusy() || backLeft.isBusy() || backRight.isBusy()) {

        }
    }
    public void liftConfig(int height, boolean ifCone) throws InterruptedException {
        int ticks = 0;
        if (!ifCone) {
            if (height == 3) {
                ticks = (int) (liftTicks * highLift);

            } else if (height == 2)  {
                ticks = (int) (liftTicks * mediumLift);
            } else if (height == 1) {
                ticks = (int) (liftTicks * lowLift);
            } else {
                ticks = 0;
            }
        } else {
            ticks = (int) (liftTicks * (10 - (1.5 * countCones)));
            countCones++;
        }

        lift1.setTargetPosition(ticks);
        lift2.setTargetPosition(ticks);
        lift1.setPower(liftSpeed);
        lift2.setPower(liftSpeed);
        lift1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        lift2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }
    public void armPresets(int degrees) {
        if (degrees > 180) {
            arm.setPosition(2/3);
        } else {
            arm.setPosition(degrees / 180 * 2/3);
        }

    }


}


