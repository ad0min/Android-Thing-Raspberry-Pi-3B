# Iot Lab 3 and lab 4 - Using RC522

## Member

1. Nguyen Thanh Dat - 1510700
2. Duong Vong - 1514090
3. Tran Quoc Khanh - 1511524
4. Huynh Thanh Duy - Update later

## Feature

- Write data to card
- Read data from card
- Flash Blue Led indicates the system is waiting for card
- Flash Red Led indicates that the invalid card
- When a valid card is swiped, the system will send the card data to server and it will turn on the Blue Led 5 seconds
- To view the result, visit link [Result](http://demo1.chipfc.com/SensorValue/List/7). If you see a record has value 12365425.00, it works correctly.

## Setup hardware

1. BCM4 pin was connected to Blue Led
2. BCM17 pin was connected to Red Led
3. Other pins were connected as instruction of Lab 3