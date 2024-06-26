Evaluation Boards/Products Supported
------------------------------------ 
-Products supported: AD7190, AD7192, AD7193, AD7194, AD7195

Overview
--------
This is a IIO based firmware application to evalute the AD719x device.
This code was developed and tested on SDP-K1 controller board: https://os.mbed.com/platforms/SDP_K1/.
Use of Mbed platform allows code to port on other Mbed supported target boards with little or no modifications.

Product details: https://www.analog.com/en/products/ad7193.html
User Guide for this code: https://wiki.analog.com/resources/tools-software/product-support-software/ad719x_mbed_iio_application
Communication Protocol: SPI


Hardware Setup
--------------
Required: SDP-K1 (or alternative Mbed enabled controller board), EVAL-AD719x board and USB cable.
Plug in the EVAL-AD719x board on SDP-K1 board (or any other Mbed enabled controller board) 
using the SDP-120 or Arduino or PMOD on-board connector (refer software wiki page to identify suitable interface).
Connect SDP-K1 board to the PC using the USB cable.


How to Get Started
------------------
Mbed web/online compiler: https://studio.keil.arm.com/auth/login/
Import code into compiler and compile it to generate executable binary file. 
Drag and drop binary file into USB drive hosted by SDP-K1 controller board. 
Find detailed instructions here: https://wiki.analog.com/resources/tools-software/product-support-software/pcg-fw-mbed-build-guide


Notes
-----
A detailed user guide on SDP-K1 controller board is available here:
https://os.mbed.com/platforms/SDP_K1/
https://www.analog.com/en/design-center/evaluation-hardware-and-software/evaluation-boards-kits/SDP-K1.html.

Copyright (c) 2023 Analog Devices, Inc.  All rights reserved.

