package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;


@Autonomous
public class LiftTestRR extends LinearOpMode {
    public void runOpMode() throws InterruptedException {

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        waitForStart();

        while (opModeIsActive()) {
            drive.liftConfig(3, false);
            sleep(500);
            drive.liftConfig(0, false);
            sleep(500);
            drive.liftConfig(0, true);
            sleep(500);
            drive.liftConfig(0, false);
            sleep(500);
        }


    }
}


