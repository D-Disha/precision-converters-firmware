/***************************************************************************//*
 * @file    ad4170_temperature_sensor.h
 * @brief   AD4170 temperature sensor module global defines
 * @details
******************************************************************************
 * Copyright (c) 2021 Analog Devices, Inc. All Rights Reserved.
 *
 * This software is proprietary to Analog Devices, Inc. and its licensors.
 * By using this software you agree to the terms of the associated
 * Analog Devices Software License Agreement.
******************************************************************************/

#ifndef AD4170_TEMPERATURE_SENSOR_H_
#define AD4170_TEMPERATURE_SENSOR_H_

#ifdef __cplusplus
extern "C"
{
#endif //  _cplusplus

/******************************************************************************/
/***************************** Include Files **********************************/
/******************************************************************************/

#include <stdint.h>

/******************************************************************************/
/********************* Macros and Constants Definitions ***********************/
/******************************************************************************/

/******************************************************************************/
/********************** Public/Extern Declarations ****************************/
/******************************************************************************/

float get_ntc_thermistor_temperature(uint32_t ntc_sample, uint8_t chn);
float get_rtd_temperature(uint32_t rtd_sample, uint8_t chn);
float get_tc_temperature(uint32_t tc_sample, uint32_t cjc_sample,
			 uint8_t tc_chn,
			 uint8_t cjc_chn, float *cjc_temp);

#ifdef __cplusplus  // Closing extern c
}
#endif //  _cplusplus

#endif	// end of AD4170_TEMPERATURE_SENSOR_H_
