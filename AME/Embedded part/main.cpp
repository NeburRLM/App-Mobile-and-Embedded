#include "temperatura.h"
#include "mbed.h"
#include <iostream>
#include <cmath>
#include <iomanip> // Necessari per a std::setprecision
#include <sstream> // Necessari per a std::ostringstream
#include "photoresistor.h"
#include "BufferedSerial.h"




BufferedSerial luminosityBs (D1,D0,9600);


int main() {
    float tU, t;
    t = calcularTemperatura();
    char luminosityBuf[20];
    //float tempAnterior=tU;
    float lu;
    luminosityBs.set_format(
    /* bits */ 8,
    /* parity */ BufferedSerial::None,
    /* stop bit */ 1
    );
    while (1) {
        lu = calcularLux();
        printf("lux: %f \n", lu);
        t = calcularTemperatura();
        printf("temperature: %f \n", t);
        
        sprintf(luminosityBuf, " %.2f,%.2f\n", lu, t);
        luminosityBs.write(luminosityBuf, sizeof(luminosityBuf));
        ThisThread::sleep_for(1s);
    }
}