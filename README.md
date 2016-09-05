# Pinning TrustManager

###Pinning TrustManager is a [Cydia Substrate](http://www.cydiasubstrate.com) extension that implements dynamic device-based certificate pinning.

#Overview
The repository contains two different implementations:

1. [Public-Key Pinning](https://github.com/dbuhov/pinningTrustManager/tree/master/publicKeyPinning)
	Public-key pinning, as the name suggests, perfoms pinning of the public key of the certificate. If the key is changed, the user is immediately notified and have to decide whether to accept of reject this change.

2. [Notary-assisted Certificate Pinning](https://github.com/dbuhov/pinningTrustManager/tree/master/notaryAssistedPinning)
	Notary-assisted Certificate Pinning presents an enhancement of the public-key pinning approach. Instead of relying on the user to decide if a certificate change is malicious, we have ported our tool to the [ICSI Notary service](https://notary.icsi.berkeley.edu) which passivly monitors the certificates. This way we eliminate the human factor and provide proper proper security for the network communication produced by the applications.

Instructions and requirements for running the tools could be found in the folders.

#Used academically?
If you have used the public-key pinning or the notary-assisted certificate pinnning in research, plese cite the papers describing them:

1. Public-Key Pinning

	@INPROCEEDINGS{Buhov2016PinIt, 
	author={D. Buhov and M. Huber and G. Merzdovnik and E. Weippl},
	booktitle={2016 IFIP Networking Conference (IFIP Networking) and Workshops},
	title={Pin it! Improving Android network security at runtime},
	year={2016},
	pages={297-305},
	keywords={Androids;Communication networks;Face;Humanoid robots;Mobile communication;Security;Servers},
	doi={10.1109/IFIPNetworking.2016.7497238},
	month={May},}

2. Notary-assisted Certificate Pinning

	@INPROCEEDINGS{Merzdovnik2016Notaryassisted,
	  Author = {Georg Merzdovnik and Damjan Buhov and {Artemios G.} Voyiatzis and {Edgar R.} Weippl},
	  title = {Notary-assisted Certificate Pinning for Improved Security of (Android) Apps},
	  booktitle = {11th International Conference on Availability, Reliability and Security (ARES 2016)},
	  year = {2016},
	  month = {9},
	  pdf = {https://www.sba-research.org/wp-content/uploads/publications/notarypin.pdf},
	  }

#Acknowledgements
The projects were carried out at [SBA Research](https://www.sba-research.org)
