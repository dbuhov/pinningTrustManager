# Pinning TrustManager

##Notary Assisted Certificate Pinning 

###Notary assisted certificate pinning is a [Cydia Substrate](http://www.cydiasubstrate.com) extension that implements dynamic device-based certificate pinning.
This tool presents an enhancement of the [public-key pinning](https://github.com/dbuhov/pinningTrustManager/tree/master/publicKeyPinning) by porting it to the [ICSI notary](https://notary.icsi.berkeley.edu) and eliminating the need for a user interaction upon cerificate change.  
For further details, please refer to the following [paper](https://www.sba-research.org/wp-content/uploads/publications/notarypin.pdf) 

#Requirements
1. [Android Substrate SDK](http://www.cydiasubstrate.com/id/73e45fe5-4525-4de7-ac14-6016652cc1b8/)
2. Rooted Android device or Android emulator
3. [Cydia Substrate](https://play.google.com/store/apps/details?id=com.saurik.substrate) installed on the device

#Instructions
1. Import the project
2. Add the project dependency in the Java Build Path
3. Run the project
4. Restart the device

#Acknowledgements
The project was carried out at [SBA Research](https://www.sba-research.org)
