# SigmaRest
A RESTful interface shell project for Sigma and SigmaNLP

This assumes you've already installed Tomcat and SigmaKEE.  Just compile
this project with Ant and restart Tomcat.  Then invoke the following
in your browser to initialize SigmaRest

http://localhost:8080/sigmarest/resources/init

then test with

http://localhost:8080/sigmarest/resources/term?term=WearableItem

you should see a list of SUMO subclasses such as

[Sock, UniformClothing, Dishdashah, Oqal, Jewelry, SafetyHarness,
Sleeve, Cloak, Tagiyyah, Kufiyyah, Shirt, Veil, OutdoorClothing,
Abayah, Shimagh, Collar, Coat, Misbahah, Glove, Sandal, Dress,
Clothing, ProtectiveEyewear, MotorcycleGlove, Respirator, Earphone,
Apron, Niqab, Shoe, SafetyVest, Hijab, Trousers, TieClothing, Pearl,
PersonalAdornment, Khimar, Belt, Jilbab, HearingProtection, BowlingShoe,
Hat, Gutrah, Jallabiyyah]

At that point, if it's all working, you can follow a tutorial on setting
up a RESTful client as appropriate and/or expand the functionality of the
existing SigmaResource.java class to expose more of the SigmaKEE api in
RESTful form.
