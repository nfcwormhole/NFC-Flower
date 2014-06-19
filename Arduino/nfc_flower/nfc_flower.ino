#include <dht11.h>
#include <EEPROM.h>

dht11 DHT11;
#define DHT11PIN 12 //define DHT11 pin
#define RELAY 2    //define relay pin
/* Copyright 2013 Ten Wong, wangtengoo7@gmail.com  
*  
*/

/*********************************************************
** sample: when reset the rf430, it will write the uri to 
** rf430 tag.
***********************************************************/
#if ARDUINO >= 100
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif
#include <Wire.h>
#include <RF430CL330H_Shield.h>
#include <NdefMessage.h>
#include <NdefRecord.h>


//#define IRQ   (7)
//#define RESET (6)  
//use UNO
#define IRQ   (3)
#define RESET (4)
int led = 13;
RF430CL330H_Shield nfc(IRQ, RESET);

byte uri[] = "http://www.elecfreaks.com";
byte flower[] = "flower";
byte humidity[] = "humidity";
byte temperature[] = "temperature";
byte water[] = "water";
byte setting[] = "setting";

volatile byte into_fired = 0;
uint16_t flags = 0;
uint16_t read_num = 0;
uint16_t write_num = 0;
uint8_t loop_num = 0;

// start reading from the first byte (address 0) of the EEPROM
int address = 0;
byte hum;
byte temp;

void setup(void) 
{
    Serial.begin(115200);    
    Serial.println("Hello!");
    pinMode(led, OUTPUT); 
    digitalWrite(led, HIGH);
    pinMode(RELAY,OUTPUT);
    digitalWrite(RELAY,LOW);
    nfc.begin();
    if(EEPROM.read(address+2)!=1)
    {
      EEPROM.write(address, 50);//humidity
      EEPROM.write(address+1, 30);//temperature
      EEPROM.write(address+2,1);
    }
    /// init data here
    Restore_Default();//write sensor data to DNFC TAG
    //Serial.println(EEPROM.read(address));
   // Serial.println(EEPROM.read(address+1));
    //enable interrupt 1
    attachInterrupt(1, RF430_Interrupt, FALLING);
    Serial.println("Wait for read or write...");
    Serial.println("DHT11 TEST PROGRAM ");
    Serial.print("LIBRARY VERSION: ");
    Serial.println(DHT11LIB_VERSION);
}

void loop(void) 
{
    //hum = EEPROM.read(address);
    //temp = EEPROM.read(address+1);
    
    if(into_fired)
    {
        //clear control reg to disable RF
        nfc.Write_Register(CONTROL_REG, INT_ENABLE + INTO_DRIVE); 
        delay(750);
        
        //read the flag register to check if a read or write occurred
        flags = nfc.Read_Register(INT_FLAG_REG); 
        //Serial.print("INT_FLAG_REG = 0x");Serial.println(flags, HEX);

        //ACK the flags to clear
        nfc.Write_Register(INT_FLAG_REG, EOW_INT_FLAG + EOR_INT_FLAG); 

        if(flags & EOW_INT_FLAG)      //check if the tag was written
        {
            write_num++;
            Serial.print("tag be written times = ");Serial.println(write_num);
            Serial.println("33333");
            //do customer operator
            flower_operator();
            Serial.println("1111111");
            //immediately Restore Default data
            Restore_Default();
            Serial.println("222222");
        }
        else if(flags & EOR_INT_FLAG) //check if the tag was read
        {
            read_num++;
            Serial.print("tag be readed times = ");Serial.println(read_num);
            digitalWrite(led, LOW);
            delay(1000);
            digitalWrite(led, HIGH);
        }

        flags = 0;
        into_fired = 0; //we have serviced INT1

        //Configure INTO pin for active low and re-enable RF
        nfc.Write_Register(CONTROL_REG, INT_ENABLE + INTO_DRIVE + RF_ENABLE);

        //re-enable INTO
        attachInterrupt(1, RF430_Interrupt, FALLING);
    }
//
    if (DHT11.humidity < EEPROM.read(address)) //auto control relay watering
    {
      digitalWrite(RELAY,HIGH);
      delay(2000);
      digitalWrite(RELAY,LOW);
    }
    if (loop_num == 50) //5s refresh tag
    {
        Restore_Default();
        Serial.println("refersh memory");
        loop_num = 0;
    }
    else
        loop_num++;

    delay(100);
}

/**
**  @brief  interrupt service
**/
void RF430_Interrupt()            
{
    into_fired = 1;
    detachInterrupt(1);//cancel interrupt
}

boolean ByteArrayCompare(byte a[], byte b[], uint8_t array_size)
{
    for (int i = 0; i < array_size; i++)
      if (a[i] != b[i])
        return false;
    return true;
}

void Restore_Default()
{
    int chk = DHT11.read(DHT11PIN);
    NdefRecord records[4];
    /** here get data from sensor **/
    //get sensor data
    byte exData0[] = {60}; //humidity 60%  form sensor   
    byte exData1[] = {24}; //temperature 24 C  form sensor 
    byte exData2[] = {50}; //humidity 50%  warm line 50%
    exData0[0] = DHT11.humidity;
    exData1[0] = DHT11.temperature;
    Serial.println(exData0[0]);
    Serial.println(exData1[0]);
    //Serial.println( DHT11.temperature);
    records[0].createUri("http://www.elecfreaks.com");
    records[1].createExternal("flower", "humidity", exData0, sizeof(exData0));
    records[2].createExternal("flower", "temperature", exData1, sizeof(exData1));
    records[3].createExternal("flower", "setting", exData2, sizeof(exData2));
    
    NdefMessage msg(records, sizeof(records)/sizeof(NdefRecord));
    uint16_t msg_length = msg.getByteArrayLength();
    byte message[msg_length];
    msg.toByteArray(message);
    nfc.Write_NDEFmessage(message, msg_length);
}

void flower_operator()
{
    byte domin[6];
    nfc.Read_Continuous(0x32, domin, 6);
    if (ByteArrayCompare(domin, flower, 6))
    {
        byte type[10];
        nfc.Read_Continuous(0x39, type, 10);
        Serial.println("xxxxxx");
        
        if (ByteArrayCompare(type, water, sizeof(water)-1)) // -1 because do not compare '\0'
        {
            Serial.println("yyyyyy");
            if (nfc.Read_OneByte(0x3E)) 
            {
                Serial.println("water"); 
                //deal with relay
               digitalWrite(RELAY,HIGH);
               delay(2000);
               digitalWrite(RELAY,LOW);
            }
            //do water flower operate
        }
        else if (ByteArrayCompare(type, setting, sizeof(setting)-1))
        {
            Serial.println("zzzzzz");
            Serial.println("setting");
            //do modify warn line
            Serial.print("setdata:");
            Serial.println(nfc.Read_OneByte(0x40));
            EEPROM.write(address, nfc.Read_OneByte(0x40));
        }
    }
}
