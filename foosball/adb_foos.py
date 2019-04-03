import CHIP_IO.GPIO as GPIO
import time
import os
import urllib2
import socket
import logging
from subprocess import call

TIMESTR = time.strftime("%Y%m%d-%H%M%S")

logging.basicConfig(level=logging.DEBUG, filename='foos_' + TIMESTR + '.log')

TABLE_ID = "5FLOOR"

BTNRESET = "GPIO2"
BTN1 = "GPIO3"
BTN2 = "GPIO4"
LED1 = "GPIO5"
LED2 = "GPIO6"

OFF = GPIO.HIGH
ON = GPIO.LOW

FLASH_TIME = 0.2
FLASH_COUNT = 10

GPIO.setup(LED1, GPIO.OUT)
GPIO.setup(LED2, GPIO.OUT)
GPIO.output(LED1, OFF)
GPIO.output(LED2, OFF)

GPIO.setup(BTN1, GPIO.IN)
GPIO.setup(BTN2, GPIO.IN)
GPIO.setup(BTNRESET, GPIO.IN)

def printlog(message):
   print(message)
   logging.info(message)

def blink(speed, count, *leds):
    while count > 0:
        count -= 1
        for led in leds:
            GPIO.output(led, ON)
        time.sleep(speed)
        for led in leds:
            GPIO.output(led, OFF)
        time.sleep(speed)

def command(command):
    call(command, shell=True)

# Initialize
GPIO.output(LED1, ON)
GPIO.output(LED2, ON)

#Init adb
printlog("Initilizing ADB")
command('adb devices')

blink(FLASH_TIME, FLASH_COUNT, LED1, LED2)

flashing = False

def updateScore(side):
    try:
        if side == 0:
            command('adb shell am broadcast -a action_goal --es scoringSide "SIDE_1"')
        else:
            command('adb shell am broadcast -a action_goal --es scoringSide "SIDE_2"')    
        return True
    except:
        logging.exception("Update Score failure:")
        return False

def buttoncallback(button):
    global flashing
    if flashing:
        return
    flashing = True
    if button == BTN1:
        printlog("TEAM 1 GOAL!")
        GPIO.output(LED1, ON)
        if updateScore(0):
            blink(FLASH_TIME, FLASH_COUNT, LED1)
        else:
            GPIO.output(LED1, OFF)
    else:
        printlog("TEAM 2 GOAL!")
        GPIO.output(LED2, ON)
        if updateScore(1):
            blink(FLASH_TIME, FLASH_COUNT, LED2)
        else:
            GPIO.output(LED2, OFF)
    flashing = False

GPIO.add_event_detect(BTN1, GPIO.FALLING, buttoncallback)
GPIO.add_event_detect(BTN2, GPIO.FALLING, buttoncallback)

printlog ("Awaiting Button press")

try:
    GPIO.wait_for_edge(BTNRESET, GPIO.FALLING)
    printlog("Reset button pressed")
    blink(0.1, 20, LED1, LED2)
    os.system("reboot")
except:
    logging.exception("Exiting reset button loop:")
    GPIO.cleanup()

printlog("Cleaning up")

GPIO.cleanup()
