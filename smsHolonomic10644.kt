package org.firstinspires.ftc.teamcode

/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.graphics.Color

import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.Range

import org.firstinspires.ftc.teamcode.smsHardware

@TeleOp(name = "10644", group = "Pushbot")
class smsHolonomic10644 : LinearOpMode() {

    /* Declare OpMode members. */
    internal var robot = smsHardware()   // Use a Pushbot's hardware
    internal var hsvValues = FloatArray(3)
    internal val values = hsvValues
    internal var armNominalPower = 0.3f
    internal var driveNominalPower = 0.3f
    internal var amPos: Int = 0
    internal var aePos: Int = 0
    internal var aeOffset: Int = 0
    internal var previousDPD = false
    internal var previousDPU = false
    internal var previousDPL = false
    internal var previousDPR = false


    @Override
    fun runOpMode() {

        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap, false)

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Hello driver\nI'm ready\n-------------------------")
        telemetry.update()
        var powerReducer = 0.5f
        // Wait for the game to start (driver presses PLAY)


        waitForStart()


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            val gamepad1LeftY = -gamepad1.left_stick_y
            val gamepad1LeftX = gamepad1.left_stick_x
            val gamepad1RightX = gamepad1.right_stick_x

            // holonomic formulas
            /*float FrontLeft = -gamepad1LeftY - gamepad1LeftX - gamepad1RightX;
            float FrontRight = gamepad1LeftY - gamepad1LeftX - gamepad1RightX;
            float BackRight = gamepad1LeftY + gamepad1LeftX - gamepad1RightX;
            float BackLeft = -gamepad1LeftY + gamepad1LeftX - gamepad1RightX;
            */

            var FrontRight = gamepad1LeftY
            var FrontLeft = gamepad1RightY

            val gamepad2LeftY = -gamepad2.left_stick_y
            val gamepad2RightY = -gamepad2.right_stick_y
            val gamepad2RightTrigger = gamepad2.right_trigger
            val gamepad2LeftTrigger = gamepad2.left_trigger

            // clip the right/left values so that the values never exceed +/- 1
            FrontRight = Range.clip(FrontRight, -1, 1)
            FrontLeft = Range.clip(FrontLeft, -1, 1)
            BackLeft = Range.clip(BackLeft, -1, 1)
            BackRight = Range.clip(BackRight, -1, 1)

            powerReducer = driveNominalPower
            
            /* check gamepad values */
            val err = check_gamepad(); // ln 181
            val is_err = false
            if (err != 0) {
              is_err = true
            }
            
            /* write the values to the motors */
            val write_err = write_to_motors() // ln 211
            val is_write_err = false
            if (write_err != 0) {
              is_write_err = true
            }


            //print out motor values
            telemetry.addLine().addData("front right", FrontRight).addData("front left", FrontLeft).addData("gamepad err", is_err).addData("motor right err", is_write_err)
                    
            telemetry.update()


            /*   functions   */

            fun check_gamepad() Int {
                if (gamepad1.right_trigger > 0) {
                    powerReducer = 1.0f
                }
                if (gamepad1.left_trigger > 0) {
                    powerReducer = 0.1f
                }

                // Allow driver to select Tank vs POV by pressing START
                var dpad_check = gamepad2.dpad_up
                if (dpad_check && dpad_check != previousDPU) {
                    aeOffset += 25
                }
                previousDPU = dpad_check

                dpad_check = gamepad2.dpad_down
                if (dpad_check && dpad_check != previousDPD) {
                    aeOffset -= 25
                }
                previousDPD = dpad_check

                dpad_check = gamepad2.dpad_left
                if (dpad_check && dpad_check != previousDPL) {
                    armNominalPower -= 0.05f
                }
                previousDPL = dpad_check

                dpad_check = gamepad2.dpad_right
                if (dpad_check && dpad_check != previousDPR) {
                    armNominalPower += 0.05f
                }
                previousDPR = dpad_check
                return 0
            }
        }
    }
    
    fun write_to_motors() Int {
      if (robot.frontRightDrive != null) {
        robot.frontRightDrive.setPower(FrontRight * powerReducer)
      }
      if (robot.frontLeftDrive != null) {
        robot.frontLeftDrive.setPower(FrontLeft * powerReducer)
      }

      return 0
    }

}
