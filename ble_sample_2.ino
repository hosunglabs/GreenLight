#include <SoftwareSerial.h>
 
SoftwareSerial bleSerial(2, 3); // RX, TX
 
unsigned int INTERVAL = 29;
unsigned int GREEN = 400;

String IBEACON = "AT+IBEA1";
String ADTY = "AT+ADTY3";
String RESET = "AT+RESET";

unsigned int time = INTERVAL;
boolean isGreen = false;
 
void setup()  
{
  Serial.begin(9600);
  Serial.println("--SETUP--");
  Serial.print("Green Light Interval : ");
  Serial.println(INTERVAL);
  
  Serial.println("");
  Serial.println("");
  Serial.println("--LOOP--");

  bleSerial.begin(9600);
  reset();
  delay(1000);
}
 
void loop() 
{
  int light = analogRead(A0);
  Serial.print("light : ");
  Serial.print(light);
  
  if (light < GREEN) {
    isGreen = true;  
  }
  
  if(isGreen == true)
  {
    String major;
    
    Serial.print("   time : ");
    Serial.print(time);
      
    if (time != 0)
    {
      String timeHex = String(time, HEX);
      timeHex.toUpperCase();
      if(time >= 16) {
        major = "AT+MARJ0x00" + timeHex;
      } else {
        major = "AT+MARJ0x000" + timeHex;
      }
      Serial.print("   major : ");
      Serial.print(time);
      Serial.print(", ");
      Serial.print(major);
      
      for(int i=0; i<13; i++) {
        byte b = major[i];
        bleSerial.write(b);  
      }
         
      time--;
      //Serial.print("   time : ");
      //Serial.print(millis());
    }
    else 
    {
      major = "AT+MARJ0x1000";
      Serial.print("   major : ");
      Serial.print(major);
      Serial.print("   END");
      //bleSerial.write(major);
      time = INTERVAL;
      isGreen = false;
      reset();
      delay(1000);
    }
  } 
    
  delay(985);
  Serial.println("");
}

void reset(void) {

      for(int i=0; i<8; i++) {
        byte b = IBEACON[i];
        bleSerial.write(b);  
      }
      delay(100);
      
      for(int i=0; i<8; i++) {
        byte b = ADTY[i];
        bleSerial.write(b);  
      }
      delay(100);
      
      for(int i=0; i<8; i++) {
        byte b = RESET[i];
        bleSerial.write(b);  
      }
      delay(100);
}
