  //This code was adapted from http://www.electrodragon.com/w/index.php?title=DHT11
  //Ideas retrieved from a really good instructable: http://www.howtogeek.com/108444/control-an-arduino-project-with-an-android-phone-via-bluetooth/

//You need to have the dht11.h library.

#include <dht11.h>
dht11 DHT11;
#define DHT11PIN 2
#define dht11_pin 15


byte read_dht11_dat()
{
  byte i = 0;
  byte result=0;
  for(i=0; i< 8; i++)
  {
    while (!digitalRead(dht11_pin));
    delayMicroseconds(30);
    if (digitalRead(dht11_pin) != 0 )
      bitSet(result, 7-i);
    while (digitalRead(dht11_pin));
  }
  return result;
}


int fan = 12;//arduino pin output 12
int incomingByte = 0;   // for incoming serial data
int sensorPin = A5;//Analog 5
int sensorValue = 0;
boolean powerOn = false;


// the setup routine runs once when you press reset:
void setup() { 

  pinMode(dht11_pin, OUTPUT);
  digitalWrite(dht11_pin, HIGH);
  
  // initialize the digital pin as an output.
  pinMode(fan, OUTPUT);     
  Serial.begin(9600);     // opens serial port, sets data rate to 9600 bps

}

// the loop routine runs over and over again forever:
void loop() {
  
  int chk = DHT11.read(DHT11PIN);
  
  
  
  
  
  byte dht11_dat[5];   
  byte dht11_in;
  byte i;// start condition
 
  digitalWrite(dht11_pin, LOW);
  delay(18);
  digitalWrite(dht11_pin, HIGH);
  delayMicroseconds(1);
  pinMode(dht11_pin, INPUT);
  delayMicroseconds(40);     
 
  if (digitalRead(dht11_pin))
  {
    Serial.println("dht11 start condition 1 not met"); // wait for DHT response signal: LOW
    delay(1000);
    return;
  }
  delayMicroseconds(80);
  if (!digitalRead(dht11_pin))
  {
    Serial.println("dht11 start condition 2 not met");  //wair for second response signal:HIGH
    return;
  }
 
  delayMicroseconds(80);// now ready for data reception
  for (i=0; i<5; i++)
  {  dht11_dat[i] = read_dht11_dat();}  //recieved 40 bits data. Details are described in datasheet
 
  pinMode(dht11_pin, OUTPUT);
  digitalWrite(dht11_pin, HIGH);
  byte dht11_check_sum = dht11_dat[0]+dht11_dat[2];// check check_sum
  if(dht11_dat[4]!= dht11_check_sum)
  {
    Serial.println("DHT11 checksum error");
  }
  Serial.print("Current Humidity = ");
  Serial.print(dht11_dat[0], DEC);
  Serial.print("%  ");
  Serial.print("Temperature = ");
  Serial.print(dht11_dat[2], DEC);
  Serial.println("C  ");
  delay(200); //refresh every 200 milliseconds
  
  
  
  
  

  // send data only when receive data:
    if (Serial.available() > 0) {
     // read the incoming byte:   
        incomingByte = Serial.read();
        
        //this controls the on/off of the fan.
        if (powerOn){
          digitalWrite(fan, LOW);
          powerOn = false;
        }
        else if (!powerOn){
          digitalWrite(fan, HIGH);
          powerOn = true;
        }
 
     }
}
