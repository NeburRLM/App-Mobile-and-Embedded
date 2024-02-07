#include "temperatura.h"
#include <cmath>
#include "mbed.h"
#include <iostream>

AnalogIn thermistor(A1);
float calcularTemperatura() {
      
    
    const float Rb = 100000.0;
    const float R0 = 100000.0;
    const float T0 = 298.15;
    const float K = 273.15;
    const float Beta = 4299.0;


    float ADCres = thermistor.read_u16();
    float Rt = Rb * ((65535 / ADCres) - 1.0);
    float Tk = 1.0 / (log(Rt / R0) / Beta + 1.0 / T0);

    return (Tk - K)/2;
}