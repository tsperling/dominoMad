;DOMINO MAD
;A DOMINO MAN REMAKE - by T Sperling - 2006
;----------------------------------------
;Control schemes:
;1=Cursor keys and Ctrl
;2=Mouse and Mousebutton (clicking the mouse toggles picking up / dropping - no need to hold the button down)

;Animated body count=
;1 ground
;Rigid body count=
;1 player
;bully - moves only down, and can only be pushed down
;bee - moves towards player
;bag lady - moves vertically
;trolley - moves horizontally
;golfer - moves diagonally
;golfcart - moves horizontally
;gopher - moves horizontally
;ladder - moves diagonally
;barrow - moves horizontally
;jackhammer - moves diagonally, changing direction randomly
;dog - moves horizontally

Graphics3D 800,600,0,2
ClearTextureFilters
Const FPS=60
Global cam = CreateCamera()

CameraProjMode cam,2

CameraRange cam,1,100
PositionEntity cam,0,28,-47
zoom#=0.028

l=CreateLight()
centerpoint=CreatePivot()

SeedRnd MilliSecs() ;Seed the RNG
HidePointer

;Load graphics used for DrawImage (not textures)
Global fontimage=LoadAnimImage("domfont.png",13,15,0,96) ;96 characters per colour
MaskImage fontimage,255,0,255

Global menufontimage=LoadAnimImage("menudomfont.png",26,30,0,192) ;96 characters per colour
MaskImage menufontimage,255,0,255

mouseimage=LoadImage("mousepoint.png")
MaskImage mouseimage,255,0,255

menukeyimage=LoadAnimImage("menukey.png",148,148,0,3)
MaskImage menukeyimage,255,0,255
menumouseimage=LoadAnimImage("menumouse.png",148,148,0,3)
MaskImage menumouseimage,255,0,255

;TOKAMAK SETUP
;There are a maximum of 50 dominos per level, plus 12 enemies = 62 Rigid_Bodies
;There are four walls and a floor = 5 Animated bodies
Const Rigid_Bodies=62,Animated_Bodies=5

TOKSIM_SetRigidBodiesCount Rigid_Bodies
TOKSIM_SetAnimatedBodiesCount Animated_Bodies
TOKSIM_SetRigidParticleCount 0
TOKSIM_SetControllersCount 0
TOKSIM_SetGeometriesCount Rigid_bodies+Animated_Bodies
TOKSIM_CreateSimulator(0,-20,0)

;domino dimensions:
domw#=0.275
domh#=2
domb#=1
domscale#=2

domspx#=1.5
domspz#=2

domfadespeed#=0.02

;General game setup

;Define playfield edges
playleft#=-34
playright#=34
playup#=25
playdown#=-51

playheight#=5

upwall = TOKAB_Create()
TOKAB_AddBox(upwall,(playleft*-1)+playright,playheight,1)
TOKAB_SetPosition(upwall,0,playheight/2,playup)
TOKAB_SetCollisionID(upwall,4)
testerup=CreateCube()
ScaleEntity testerup,((playleft*-1)+playright)/2,playheight/2,0.5
PositionEntity testerup,TOKAB_GetX(upwall),TOKAB_GetY(upwall),TOKAB_GetZ(upwall)
EntityAlpha testerup,0.3

downwall = TOKAB_Create()
TOKAB_AddBox(downwall,(playleft*-1)+playright,playheight,1)
TOKAB_SetPosition(downwall,0,playheight/2,playdown)
TOKAB_SetCollisionID(downwall,4)
testerdown=CreateCube()
ScaleEntity testerdown,((playleft*-1)+playright)/2,playheight/2,0.5
PositionEntity testerdown,TOKAB_GetX(downwall),TOKAB_GetY(downwall),TOKAB_GetZ(downwall)
EntityAlpha testerdown,0.3

leftwall = TOKAB_Create()
TOKAB_AddBox(leftwall,1,playheight,(playdown*-1)+playup)
TOKAB_SetPosition(leftwall,playleft,playheight/2,playup-((playdown*-1)+playup)/2)
TOKAB_SetCollisionID(leftwall,4)
testerleft=CreateCube()
ScaleEntity testerleft,0.5,playheight/2,((playdown*-1)+playup)/2
PositionEntity testerleft,TOKAB_GetX(leftwall),TOKAB_GetY(leftwall),TOKAB_GetZ(leftwall)
EntityAlpha testerleft,0.3

rightwall = TOKAB_Create()
TOKAB_AddBox(rightwall,1,playheight,(playdown*-1)+playup)
TOKAB_SetPosition(rightwall,playright,playheight/2,playup-((playdown*-1)+playup)/2)
TOKAB_SetCollisionID(rightwall,4)
testerright=CreateCube()
ScaleEntity testerright,0.5,playheight/2,((playdown*-1)+playup)/2
PositionEntity testerright,TOKAB_GetX(rightwall),TOKAB_GetY(rightwall),TOKAB_GetZ(rightwall)
EntityAlpha testerright,0.3

;fadedistance controls the distance it takes an entity to turn from alpha 1 to 0.
fadedistance#=7

;Remove Collision data between player (ID 1) and dominoes (ID 2)
;Enemies have a collision ID of 3, and the level barriers have an ID of 4
TOKSIM_SetCollisionResponse(1,2,0)
TOKSIM_SetCollisionResponse(2,1,0)
;Turn off Collision between enemies and dominoes - they should be pushed over by proximity detection alone
TOKSIM_SetCollisionResponse(3,2,0)
TOKSIM_SetCollisionResponse(2,3,0)
;Turn off collision between enemies and the wall barriers
TOKSIM_SetCollisionResponse(3,4,0)
TOKSIM_SetCollisionResponse(4,3,0)
;Turn off collision between dominoes and wall barriers
TOKSIM_SetCollisionResponse(2,4,0)
TOKSIM_SetCollisionResponse(4,2,0)

menublocktex=LoadTexture("domface.png",1)

;Sprite for blocking off the view (for menus and that kind of thing)
blocker = CreateSprite(cam)
ScaleSprite blocker,39.9,30
PositionEntity blocker,0,0,10
EntityAlpha blocker,0
HideEntity blocker
EntityOrder blocker,-1
EntityTexture blocker,menublocktex

ground = CreateFloor(90,120)
EntityColor ground,0,0,255
tokground = TOKAB_Create()
TOKAB_AddBox(tokground,80,2,140)
TOKAB_SetPosition(tokground,0,-1,-20)

;Character sprite set ups
player=CreateSprite()
ScaleSprite player,2.4,2.4
EntityFX player,17 ;backface culling disabled and full bright
playertex=LoadAnimTexture("playergraph.png",4,64,64,0,1)
EntityTexture player,playertex,0
;-
playermousetarget=CreateCube()

tokplayer = TOKRB_Create()
TOKRB_AddSphere(tokplayer,3.5)
TOKRB_SetPosition(tokplayer,0,2.3,playup-2)
TOKRB_SetLinearDamping tokplayer,0.1
TOKRB_SetAngularDamping tokplayer,0.2
TOKRB_SetMass tokplayer,20
TOKRB_SetSphereInertiaTensor tokplayer,3.5,20
TOKRB_SetCollisionID(tokplayer,1)
;-
shadowtex=LoadTexture("shadowgraph.bmp",2)
playershadow=CreateFloor(7,7)
EntityTexture playershadow,shadowtex

;The 'domino' that the player hits enemies with
fakedom=CreateCube(player)
ScaleEntity fakedom,(domw#*domscale#)/2,(domh#*domscale#)/2,(domb#*domscale#)/2
fakedomrot1#=45 :fakedomrot2#=90
fakedomx1#=-2.2 :fakedomx2#=4
fakedomy1#=3    :fakedomy2#=0
RotateEntity fakedom,0,0,fakedomrot2#
PositionEntity fakedom,fakedomx2#,fakedomy2#,0
HideEntity fakedom

hitpause#=500 ;The time it takes to hit someone with the domino
hitsizex#=5
hitsizez#=5

;-
playerspeedx#=25;1
playerspeedz#=40;1.8
;determins if the player has the 'action' button pressed
playerkey=0

;Enemy character set ups
;The bully and bee are separate from the other enemies, as they always start from the same point
;Bully
bully=CreateSprite()
ScaleSprite bully,2.4,4.8
;PositionEntity bully,-8,4.7,0
EntityFX bully,1
bullytex=LoadAnimTexture("bullygraph.png",4,64,128,0,1)
EntityTexture bully,bullytex,0
;-
tokbully = TOKRB_Create()
TOKRB_AddSphere(tokbully,4.6)
;TOKRB_SetPosition(tokbully,-8,2.2,0)
TOKRB_SetLinearDamping tokbully,0.0001
TOKRB_SetAngularDamping tokbully,0.002
TOKRB_SetMass tokbully,500
TOKRB_SetSphereInertiaTensor tokbully,5,20
TOKRB_SetCollisionID(tokbully,3)
bullyspeed#=-2
;The radius around the bully that he kicks dominoes
bullyspx#=2.7
bullyspz#=3.2
;-
bullyshadow=CreateFloor(7,7)
EntityTexture bullyshadow,shadowtex
HideEntity bullyshadow
EntityAlpha bullyshadow,0
;Deactivate enemy
;TOKRB_SetPosition(tokbully,-80,-30,0)
TOKRB_GravityEnable tokbully,False
EntityAlpha bully,0
HideEntity bully
bullydazeon=0 : bullydazetime=0

;Bee
bee=CreateSprite()
ScaleSprite bee,2.25,1.35
beeheight#=3
EntityFX bee,1
beetex=LoadAnimTexture("beegraph.png",4,60,36,0,1)
EntityTexture bee,beetex,0
;The bee doesn't need a tok object. Hooray!
beespeed#=0.05
;The radius
beespx#=2.7
beespz#=3.2
beeshadow=CreateFloor(3,3)
EntityTexture beeshadow,shadowtex
beestingsizex#=2
beestingsizez#=3

;Money Bag (generated when the bee is hit)
money=CreateSprite()
ScaleSprite money,1.5,1.5
moneyheight#=1.3
moneytex=LoadTexture("moneybag.png",4)
EntityTexture money,moneytex
moneypicksizex#=2
moneypicksizez#=3
moneyvalue=500

;Clock
clock=CreateSprite()
ScaleSprite clock,2.2,2.2
clocktex=LoadAnimTexture("clockgraph.png",4,64,64,0,1)
EntityTexture clock,clocktex,0
clockstartx#=32 : clockstarty#=2.1 : clockstartz#=20
PositionEntity clock,clockstartx,clockstarty,clockstartz

;Other enemy characters (get lumped together)
;levelenemies(type of level, enemy, enemy type, enemyradius (1=x, 2=z), enemyspeed (1=speed, 2=switchspeed - speed at which the enemy changes direction, 3=gamespeed - speed the enemy is travelling ingame))
;ENEMY TYPE GUIDE (if more than 6, signals enemy is active)
;--INACTIVE
;1 = vertical, 2 = horizontal, 3 = diagonal
;4 = vertical, stops randomly, 5 = horizontal, stops randomly, 6 = diagonal, changes direction randomly
;--ACTIVE
;7 = vertical, 8 = horizontal, 9 = diagonal
;10 = vertical, stops randomly, 11 = horizontal, stops randomly, 12 = diagonal, changes direction randomly

;toklevelenemies(type of level, tokenemy)
Dim levelenemies#(3,4,1,2,3)
Dim toklevelenemies(3,4)
Dim enemyheight#(3,4)
Dim shadowenemies(4) ;each enemy on the level gets a shadow object (not including bully and bee)

Dim diagdetails#(3,4,1,1) ;diagonal type enemies need a separate x and z speed (diagdetails(leveltype,enemynum,xspeed,zspeed)

enemydazepause#=5000

Dim enemydaze#(4,1) ;(enemynum,0)=dazed status (0=undazed, 1=dazed, 2=enemy pause), (enemynum,1)=dazed time

;Shadow objects for other enemies
For i = 1 To 4
	shadowenemies(i)=CreateFloor(7,7)
	EntityTexture shadowenemies(i),shadowtex
	HideEntity shadowenemies(i)
	EntityAlpha shadowenemies(i),0
Next

;-TYPE 1
;Bag Lady
levelenemies(1,1,0,0,0)=CreateSprite()
levelenemies(1,1,1,0,0)=1 ;vertical type enemy
ScaleSprite levelenemies(1,1,0,0,0),2.4,2.4
enemyheight(1,1)=2.3
;-
EntityFX levelenemies(1,1,0,0,0),1
bagladytex=LoadAnimTexture("bagladygraph.png",4,64,64,0,1)
EntityTexture levelenemies(1,1,0,0,0),bagladytex,0
EntityAlpha levelenemies(1,1,0,0,0),0
;-
toklevelenemies(1,1) = TOKRB_Create()
TOKRB_AddBox(toklevelenemies(1,1),5,5,4)
TOKRB_SetLinearDamping toklevelenemies(1,1),0.1
TOKRB_SetAngularDamping toklevelenemies(1,1),0.2
TOKRB_SetMass toklevelenemies(1,1),10
TOKRB_SetBoxInertiaTensor toklevelenemies(1,1),5,5,5,10
TOKRB_SetCollisionID(toklevelenemies(1,1),3)
levelenemies(1,1,1,0,1)=2 ;speed
levelenemies(1,1,1,0,2)=0.9 ;switchspeed
levelenemies(1,1,1,1,0)=3.5 ;radiusx
levelenemies(1,1,1,2,0)=3.2 ;radiusz
;Deactivate enemy
TOKRB_SetPosition(toklevelenemies(1,1),-100,-100,-100)
TOKRB_GravityEnable toklevelenemies(1,1),False
HideEntity levelenemies(1,1,0,0,0)

;-
;Trolley Guy
levelenemies(1,2,0,0,0)=CreateSprite()
levelenemies(1,2,1,0,0)=2 ;horizontal type enemy
ScaleSprite levelenemies(1,2,0,0,0),3,2.4
enemyheight(1,2)=2.3
;-
EntityFX levelenemies(1,2,0,0,0),1
trolleytex=LoadAnimTexture("trolleygraph.png",4,80,64,0,1)
EntityTexture levelenemies(1,2,0,0,0),trolleytex,0
EntityAlpha levelenemies(1,2,0,0,0),0
;-
toklevelenemies(1,2) = TOKRB_Create()
TOKRB_AddBox(toklevelenemies(1,2),6.2,5,4)
TOKRB_SetLinearDamping toklevelenemies(1,2),0.1
TOKRB_SetAngularDamping toklevelenemies(1,2),0.2
TOKRB_SetMass toklevelenemies(1,2),10
TOKRB_SetBoxInertiaTensor toklevelenemies(1,2),5,5,5,10
TOKRB_SetCollisionID(toklevelenemies(1,2),3)
levelenemies(1,2,1,0,1)=1.5 ;speed
levelenemies(1,2,1,0,2)=0.9 ;switchspeed
levelenemies(1,2,1,1,0)=4 ;radiusx
levelenemies(1,2,1,2,0)=3.2 ;radiusz
;Deactivate enemy
TOKRB_SetPosition(toklevelenemies(1,2),-100,-100,-100)
TOKRB_GravityEnable toklevelenemies(1,2),False
HideEntity levelenemies(1,2,0,0,0)

;-
;Dog

;-TYPE 2
;Golfer
levelenemies(2,1,0,0,0)=CreateSprite()
levelenemies(2,1,1,0,0)=3 ;diagonal type enemy
ScaleSprite levelenemies(2,1,0,0,0),3,3
enemyheight(2,1)=3
;-
EntityFX levelenemies(2,1,0,0,0),1 ;diagonal types don't need backfacing culling disabled, as they don't flip
golfertex=LoadAnimTexture("golfergraph.png",4,80,81,0,1)
EntityTexture levelenemies(2,1,0,0,0),golfertex,0
EntityAlpha levelenemies(2,1,0,0,0),0
;-
toklevelenemies(2,1) = TOKRB_Create()
TOKRB_AddBox(toklevelenemies(2,1),3,5,4)
TOKRB_SetLinearDamping toklevelenemies(2,1),0.1
TOKRB_SetAngularDamping toklevelenemies(2,1),0.2
TOKRB_SetMass toklevelenemies(2,1),10
TOKRB_SetBoxInertiaTensor toklevelenemies(2,1),5,5,5,10
TOKRB_SetCollisionID(toklevelenemies(2,1),3)
levelenemies(2,1,1,0,1)=1.5 ;speed (maxspeed for diagonal enemies)
levelenemies(2,1,1,0,2)=0.9 ;switchspeed
levelenemies(2,1,1,1,0)=2.5 ;radiusx
levelenemies(2,1,1,2,0)=2.8 ;radiusz
;Deactivate enemy
TOKRB_SetPosition(toklevelenemies(2,1),-100,-100,-100)
TOKRB_GravityEnable toklevelenemies(2,1),False
HideEntity levelenemies(2,1,0,0,0)

;-
;Golf Cart
levelenemies(2,2,0,0,0)=CreateSprite()
levelenemies(2,2,1,0,0)=2 ;horizontal type
ScaleSprite levelenemies(2,2,0,0,0),3.225,3.45
enemyheight(2,2)=3.4
;-
EntityFX levelenemies(2,2,0,0,0),1
golfcarttex=LoadAnimTexture("golfcartgraph.png",4,86,92,0,1)
EntityTexture levelenemies(2,2,0,0,0),golfcarttex,0
EntityAlpha levelenemies(2,2,0,0,0),0
;-
toklevelenemies(2,2) = TOKRB_Create()
TOKRB_AddBox(toklevelenemies(2,2),6.2,5,4)
TOKRB_SetLinearDamping toklevelenemies(2,2),0.1
TOKRB_SetAngularDamping toklevelenemies(2,2),0.2
TOKRB_SetMass toklevelenemies(2,2),10
TOKRB_SetBoxInertiaTensor toklevelenemies(2,2),5,5,5,10
TOKRB_SetCollisionID(toklevelenemies(2,2),3)
levelenemies(2,2,1,0,1)=1.5 ;speed (maxspeed for diagonal enemies)
levelenemies(2,2,1,0,2)=0.9 ;switchspeed
levelenemies(2,2,1,1,0)=4.2 ;radiusx
levelenemies(2,2,1,2,0)=3.2 ;radiusz
;Deactivate enemy
TOKRB_SetPosition(toklevelenemies(2,2),-100,-100,-100)
TOKRB_GravityEnable toklevelenemies(2,2),False
HideEntity levelenemies(2,2,0,0,0)

;-
;Gopher
levelenemies(2,3,0,0,0)=CreateSprite()
levelenemies(2,3,1,0,0)=5 ;horizontal type, who stops at random intervals
ScaleSprite levelenemies(2,3,0,0,0),2.4,2.4
enemyheight(2,3)=2.3
;-
EntityFX levelenemies(2,3,0,0,0),1
gophertex=LoadAnimTexture("gophergraph.png",4,64,64,0,1)
EntityTexture levelenemies(2,3,0,0,0),gophertex,0
EntityAlpha levelenemies(2,3,0,0,0),0
;-
toklevelenemies(2,3) = TOKRB_Create()
TOKRB_AddBox(toklevelenemies(2,3),5,5,4)
TOKRB_SetLinearDamping toklevelenemies(2,3),0.1
TOKRB_SetAngularDamping toklevelenemies(2,3),0.2
TOKRB_SetMass toklevelenemies(2,3),10
TOKRB_SetBoxInertiaTensor toklevelenemies(2,3),5,5,5,10
TOKRB_SetCollisionID(toklevelenemies(2,3),3)
levelenemies(2,3,1,0,1)=1.5 ;speed (maxspeed for diagonal enemies)
levelenemies(2,3,1,0,2)=0.9 ;switchspeed
levelenemies(2,3,1,1,0)=3.5 ;radiusx
levelenemies(2,3,1,2,0)=3.2 ;radiusz
;Deactivate enemy
TOKRB_SetPosition(toklevelenemies(2,3),-100,-100,-100)
TOKRB_GravityEnable toklevelenemies(2,3),False
HideEntity levelenemies(2,3,0,0,0)

;-
;Dog

;-TYPE 3
;Jackhammer
levelenemies(3,1,0,0,0)=CreateSprite()
levelenemies(3,1,1,0,0)=6 ;diagonal type enemy who changes direction randomly
ScaleSprite levelenemies(3,1,0,0,0),3,3
enemyheight(3,1)=3
;-
EntityFX levelenemies(3,1,0,0,0),1 ;diagonal types don't need backfacing culling disabled, as they don't flip
jackhammertex=LoadAnimTexture("jackhammergraph.png",4,80,81,0,1)
EntityTexture levelenemies(3,1,0,0,0),jackhammertex,0
EntityAlpha levelenemies(3,1,0,0,0),0
;-
toklevelenemies(3,1) = TOKRB_Create()
TOKRB_AddBox(toklevelenemies(3,1),3,5,4)
TOKRB_SetLinearDamping toklevelenemies(3,1),0.1
TOKRB_SetAngularDamping toklevelenemies(3,1),0.2
TOKRB_SetMass toklevelenemies(3,1),10
TOKRB_SetBoxInertiaTensor toklevelenemies(3,1),5,5,5,10
TOKRB_SetCollisionID(toklevelenemies(3,1),3)
levelenemies(3,1,1,0,1)=1.5 ;speed (maxspeed for diagonal enemies)
levelenemies(3,1,1,0,2)=0.9 ;switchspeed
levelenemies(3,1,1,1,0)=3.5 ;radiusx
levelenemies(3,1,1,2,0)=3.2 ;radiusz
;Deactivate enemy
TOKRB_SetPosition(toklevelenemies(3,1),-100,-100,-100)
TOKRB_GravityEnable toklevelenemies(3,1),False
HideEntity levelenemies(3,1,0,0,0)

;-
;Ladder

;-
;Barrow

;-
;Dog


enemywaittime#=3000 ;time a pausing enemy will pause for
enemydirectionchange=200 ;a random number between 1 and this is chosen. The lower the number, the more frequently the enemy will change direction.

;-
levelbackdrop=CreateCube()
ScaleEntity levelbackdrop,37,6,0.2
PositionEntity levelbackdrop,0,6,34
;Load backdrop textures into array
Dim leveltex(3)

leveltex(1)=LoadTexture("level1back.png",1)
leveltex(2)=LoadTexture("level2back.png",1)
leveltex(3)=LoadTexture("level3back.png",1)


.mainmenu
;-----
;The main menu of the game
;-----
;Load Config file
;If the config file doesn't exist, load the default values
If FileType("config.dmm")=0
	controlscheme=1 ;1 = keyboard, 2=mouse
	toggle=0 ;0 = off, 1 = on
	gamespeed=3 ;1= 50%, 2=75%, 3=100%
Else
;Otherwise, open the file and read the values from it
	settingsfile=ReadFile("config.dmm")
	;Read the config settings from the file
	controlscheme=ReadInt(settingsfile)
	toggle=ReadInt(settingsfile)
	gamespeed=ReadInt(settingsfile)
	;Close the level data file
	CloseFile(settingsfile)
EndIf

period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

EntityAlpha blocker,1
ShowEntity blocker

Dim menutext$(2)
Dim menuoption(2,1)

menutext(0)="Start Game"
menuoption(0,0)=300
menuoption(0,1)=1
menutext(1)="Options"
menuoption(1,0)=340
menuoption(1,1)=0
menutext(2)="Exit Game"
menuoption(2,0)=380
menuoption(2,1)=0

MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(0,0)+10

Repeat

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
		For k=1 To ticks
			time=time+period
			If k=ticks Then
				CaptureWorld
				
				;Scroll background
				upos#=upos#+0.001
				vpos#=vpos#+0.0005
				PositionTexture menublocktex,upos#,vpos#
							
				;Cursor Control for menu (using keyhit rather than keydown)
				If KeyHit(200) And KeyDown(208)<>1 ;Up
					;Depending on what is currently being selected
					If menuoption(0,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(2,0)+10 ;Start game
					If menuoption(1,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(0,0)+10 ;Options
					If menuoption(2,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(1,0)+10 ;Exit Game
				EndIf
				If KeyHit(208) And KeyDown(200)<>1 ;Down
					;Depending on what is currently being selected
					If menuoption(0,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(1,0)+10 ;Start game
					If menuoption(1,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(2,0)+10 ;Options
					If menuoption(2,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(0,0)+10 ;Exit Game
				EndIf
				;If KeyHit(203) Then ;Left
				;If KeyHit(205) Then ;Right
								
				For i=0 To 2
					;Mouse Control for menu
					If MouseX()>((GraphicsWidth()/2)-((Len(menutext(0))*26)/2))-30 And MouseX()<((GraphicsWidth()/2)+((Len(menutext(0))*26)/2))+30 And MouseY()>menuoption(i,0) And MouseY()<menuoption(i,0)+30
						menuoption(i,1)=1
						;Turn off the other two options
						If i=0 Then menuoption(1,1)=0 : menuoption(2,1)=0
						If i=1 Then menuoption(0,1)=0 : menuoption(2,1)=0
						If i=2 Then menuoption(0,1)=0 : menuoption(1,1)=0
					EndIf
				Next
				
				;Menu item selection
				;Keyboard Control (pressing either Enter, Ctrl or Space selects items)
				If KeyDown(200)<>1 And KeyDown(208)<>1 And KeyDown(203)<>1 And KeyDown(205)<>1 ;making sure no other keys are being pressed
					;Pressing ESC
					If KeyHit(1) Then Goto exitgame
					If KeyHit(28) Or KeyHit(29)  Or KeyHit(57)
						If menuoption(0,1)=1 Then Goto newgame
						If menuoption(1,1)=1 Then optioncamefrom$="mainmenu" : Goto optionmenu
						If menuoption(2,1)=1 Then Goto exitgame
					EndIf

				;Mouse Control (left clicking selects items)
					If MouseHit(1)
						If menuoption(0,1)=1 Then Goto newgame
						If menuoption(1,1)=1 Then optioncamefrom$="mainmenu" : Goto optionmenu
						If menuoption(2,1)=1 Then Goto exitgame
					EndIf
				EndIf
								
			EndIf
			
		Next

	CameraZoom cam,zoom#
	PointEntity cam,centerpoint

	RenderWorld tween
	
	txt(10,10,"Domino Mad",0)
	txt(10,30,"Main Menu",0)
	
	For i= 0 To 2
		menutxt(0,menuoption(i,0),menutext(i),menuoption(i,1))
	Next
	
	DrawImage mouseimage,MouseX(),MouseY()
	
	Flip False

Forever

.optionmenu
;-----
;The options menu of the game
;-----
period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

EntityAlpha blocker,1
ShowEntity blocker

Dim menutext$(3)
Dim menuoption(3,1)

menutext(0)="Control Method"
menuoption(0,0)=50
menuoption(0,1)=1
If toggle=0 Then menutext(1)="Toggle mode off"
If toggle=1 Then menutext(1)="Toggle mode on"
menuoption(1,0)=280
menuoption(1,1)=0
menutext(2)="Game Speed"
menuoption(2,0)=340
menuoption(2,1)=0
menutext(3)="Back to Main Menu"
menuoption(3,0)=500
menuoption(3,1)=0

If gamespeed=1 Then percent50=1 : percent75=0 : percent100=0
If gamespeed=2 Then percent50=0 : percent75=1 : percent100=0
If gamespeed=3 Then percent50=0 : percent75=0 : percent100=1

If controlscheme=1 Then MoveMouse (GraphicsWidth()/2)-50,menuoption(0,0)+110 ElseIf controlscheme=2 Then MoveMouse (GraphicsWidth()/2)+50+188,menuoption(0,0)+110 ;Toggle

Repeat

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
		For k=1 To ticks
			time=time+period
			If k=ticks Then
				CaptureWorld
				
				;Scroll background
				upos#=upos#+0.001
				vpos#=vpos#+0.0005
				PositionTexture menublocktex,upos#,vpos#
							
				;Cursor Control for menu (using keyhit rather than keydown)
				If KeyHit(200) And KeyDown(208)<>1 ;Up
					;Depending on what is currently being selected
					If menuoption(0,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(3,0)+10 ;Control Method
					If menuoption(1,1)=1
						If controlscheme=1 Then MoveMouse (GraphicsWidth()/2)-50,menuoption(0,0)+110 ElseIf controlscheme=2 Then MoveMouse (GraphicsWidth()/2)+50+188,menuoption(0,0)+110 ;Toggle
					EndIf
					If menuoption(2,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(1,0)+10 ;Game Speed
					If menuoption(3,1)=1
						If gamespeed=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-350,menuoption(2,0)+65 ElseIf gamespeed=2 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-170,menuoption(2,0)+65 ElseIf gamespeed=3 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(2,0)+65 ;Back to main
					EndIf
				EndIf
				If KeyHit(208) And KeyDown(200)<>1 ;Down
					;Depending on what is currently being selected
					If menuoption(0,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(1,0)+10 ;Control Method
					If menuoption(1,1)=1
						If gamespeed=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-350,menuoption(2,0)+65 ElseIf gamespeed=2 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-170,menuoption(2,0)+65 ElseIf gamespeed=3 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(2,0)+65 ;Toggle
					EndIf
					If menuoption(2,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(3,0)+10 ;Game Speed
					If menuoption(3,1)=1
						If controlscheme=1 Then MoveMouse (GraphicsWidth()/2)-50,menuoption(0,0)+110 ElseIf controlscheme=2 Then MoveMouse (GraphicsWidth()/2)+50+188,menuoption(0,0)+110 ;Back to main
					EndIf
				EndIf
				If KeyHit(203) ;Left
					If menuoption(0,1)=1
						If controlscheme=2 Then controlscheme=1 : MoveMouse (GraphicsWidth()/2)-50,menuoption(0,0)+110
					EndIf
					If menuoption(1,1)=1
						If toggle=0 Then toggle=1 : menutext(1)="Toggle mode on" ElseIf toggle=1 Then toggle=0 : menutext(1)="Toggle mode off"
					EndIf
					If menuoption(2,1)=1
						If gamespeed=3 Then gamespeed=2 : percent75=1 : percent100=0 : MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-170,menuoption(2,0)+65 ElseIf gamespeed=2 Then gamespeed=1 : percent50=1 : percent75=0 : MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-350,menuoption(2,0)+65
					EndIf
				EndIf
				If KeyHit(205) ;Right
					If menuoption(0,1)=1
						If controlscheme=1 Then controlscheme=2 : MoveMouse (GraphicsWidth()/2)+50+188,menuoption(0,0)+110
					EndIf
					If menuoption(1,1)=1
						If toggle=0 Then toggle=1 : menutext(1)="Toggle mode on" ElseIf toggle=1 Then toggle=0 : menutext(1)="Toggle mode off"
					EndIf
					If menuoption(2,1)=1
						If gamespeed=1 Then gamespeed=2 : percent50=0 : percent75=1 : MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)-170,menuoption(2,0)+65 ElseIf gamespeed=2 Then gamespeed=3 : percent75=0 : percent100=1 : MoveMouse (GraphicsWidth()/2)+((Len(menutext(3))*26)/2)+20,menuoption(2,0)+65
					EndIf
				EndIf
												
				;Mouse Control - Control Scheme
				If MouseX()>((GraphicsWidth()/2)-((Len(menutext(3))*26)/2))-30 And MouseX()<((GraphicsWidth()/2)+((Len(menutext(3))*26)/2))+30 And MouseY()>menuoption(0,0) And MouseY()<menuoption(0,0)+200
					menuoption(0,1)=1
					;Turn off the other three options
					menuoption(1,1)=0 : menuoption(2,1)=0 : menuoption(3,1)=0
					;Highlighting the sub-choices
					If MouseX()>(GraphicsWidth()/2)-148-70 And MouseX()<(GraphicsWidth()/2)-70 And MouseY()>menuoption(0,0)+40
						keyhighlight=1
						mousehighlight=0
					Else
						keyhighlight=0
					EndIf
					If MouseX()>(GraphicsWidth()/2)+70 And MouseX()<(GraphicsWidth()/2)+148+70 And MouseY()>menuoption(0,0)+40
						keyhighlight=0
						mousehighlight=1
					Else
						mousehighlight=0
					EndIf
				Else
					keyhighlight=0
					mousehighlight=0
				EndIf
				;Toggle Mode
				If MouseX()>((GraphicsWidth()/2)-((Len(menutext(3))*26)/2))-30 And MouseX()<((GraphicsWidth()/2)+((Len(menutext(3))*26)/2))+30 And MouseY()>menuoption(1,0) And MouseY()<menuoption(1,0)+30
					menuoption(1,1)=1
					;Turn off the other three options
					menuoption(0,1)=0 : menuoption(2,1)=0 : menuoption(3,1)=0
				EndIf
				;Game Speed
				If MouseX()>((GraphicsWidth()/2)-((Len(menutext(3))*26)/2))-30 And MouseX()<((GraphicsWidth()/2)+((Len(menutext(3))*26)/2))+30 And MouseY()>menuoption(2,0) And MouseY()<menuoption(2,0)+100
					menuoption(2,1)=1
					;Turn off the other three options
					menuoption(0,1)=0 : menuoption(1,1)=0 : menuoption(3,1)=0
					;Highlighting the sub-choices
					If MouseX()>(GraphicsWidth()/2)-180-50 And MouseX()<(GraphicsWidth()/2)-140 And MouseY()>menuoption(2,0)+50 And MouseY()<menuoption(2,0)+90
						percent50=1
					ElseIf gamespeed <> 1
						percent50=0
					EndIf
					If MouseX()>(GraphicsWidth()/2)-50 And MouseX()<(GraphicsWidth()/2)+40 And MouseY()>menuoption(2,0)+50 And MouseY()<menuoption(2,0)+90
						percent75=1
					ElseIf gamespeed <> 2
						percent75=0
					EndIf
					If MouseX()>(GraphicsWidth()/2)+115 And MouseX()<(GraphicsWidth()/2)+225 And MouseY()>menuoption(2,0)+50 And MouseY()<menuoption(2,0)+90
						percent100=1
					ElseIf gamespeed <> 3
						percent100=0
					EndIf
				EndIf
				;Back to Main Menu
				If MouseX()>((GraphicsWidth()/2)-((Len(menutext(3))*26)/2))-30 And MouseX()<((GraphicsWidth()/2)+((Len(menutext(3))*26)/2))+30 And MouseY()>menuoption(3,0) And MouseY()<menuoption(3,0)+30
					menuoption(3,1)=1
					;Turn off the other three options
					menuoption(0,1)=0 : menuoption(1,1)=0 : menuoption(2,1)=0
				EndIf
				
				
				;Menu item selection
				;Keyboard Control (pressing either Enter, Ctrl or space selects items)
				If KeyDown(200)<>1 And KeyDown(208)<>1 And KeyDown(203)<>1 And KeyDown(205)<>1 ;making sure no other keys are being pressed
					If KeyHit(1) ;Esc
						settingsfile=WriteFile("config.dmm")
						;Write the config settings from the file
						WriteInt(settingsfile,controlscheme)
						WriteInt(settingsfile,toggle)
						WriteInt(settingsfile,gamespeed)
						CloseFile(settingsfile)
						
						Goto mainmenu
					EndIf
					If KeyHit(28) Or KeyHit(29) Or KeyHit(57)
						If menuoption(1,1)=1
							If toggle=0 Then toggle=1 : menutext(1)="Toggle mode on" ElseIf toggle=1 Then toggle=0 : menutext(1)="Toggle mode off"
						EndIf
						If menuoption(3,1)=1
							settingsfile=WriteFile("config.dmm")
							;Write the config settings from the file
							WriteInt(settingsfile,controlscheme)
							WriteInt(settingsfile,toggle)
							WriteInt(settingsfile,gamespeed)
							CloseFile(settingsfile)
							;Go wherever the options menu was called
							If optioncamefrom="mainmenu" Then Goto mainmenu ElseIf optioncamefrom="game" Then Goto pausegame
						EndIf
					EndIf

				;Mouse Control (left clicking selects items)
					If MouseHit(1)
						If menuoption(0,1)=1
							If keyhighlight=1 And mousehighlight=0 Then controlscheme=1
							If keyhighlight=0 And mousehighlight=1 Then controlscheme=2
						EndIf
						If menuoption(1,1)=1
							If toggle=0 Then toggle=1 : menutext(1)="Toggle mode on" ElseIf toggle=1 Then toggle=0 : menutext(1)="Toggle mode off"
						EndIf
						If menuoption(2,1)=1
							If percent50=1 And gamespeed <> 1 : gamespeed=1
							ElseIf percent75=1 And gamespeed <> 2 : gamespeed=2
							ElseIf percent100=1 And gamespeed <> 3 : gamespeed=3
							EndIf
						EndIf
						If menuoption(3,1)=1
							settingsfile=WriteFile("config.dmm")
							;Write the config settings from the file
							WriteInt(settingsfile,controlscheme)
							WriteInt(settingsfile,toggle)
							WriteInt(settingsfile,gamespeed)
							CloseFile(settingsfile)
							;Go to wherever the options menu was called
							If optioncamefrom="mainmenu" Then Goto mainmenu ElseIf optioncamefrom="game" Then Goto pausegame
						EndIf
					EndIf
				EndIf
								
			EndIf
			
		Next

	CameraZoom cam,zoom#
	PointEntity cam,centerpoint

	RenderWorld tween
	
	txt(10,10,"Domino Mad",0)
	txt(10,30,"Options Menu",0)
	
	For i= 0 To 3
		menutxt(0,menuoption(i,0),menutext(i),menuoption(i,1))
	Next

	If controlscheme=1 Then DrawImage menukeyimage,(GraphicsWidth()/2)-148-70,100,2 ElseIf controlscheme=2 Then DrawImage menukeyimage,(GraphicsWidth()/2)-148-70,100,keyhighlight
	If controlscheme=2 Then DrawImage menumouseimage,(GraphicsWidth()/2)+70,100,2 ElseIf controlscheme=1 Then DrawImage menumouseimage,(GraphicsWidth()/2)+70,100,mousehighlight
	
	menutxt(180,400,"50%",percent50)
	menutxt(GraphicsWidth()/2-39,400,"75%",percent75)
	menutxt(520,400,"100%",percent100)
	
	DrawImage mouseimage,MouseX(),MouseY()
	
	Flip False

Forever


.newgame

;Load game options from file
;gamespeed is rated 1 to 3 (normal), and controls the speed of the game. 1=50%,2=75%,3=100%
;controlscheme=1
;toggle=0
;gamespeed=3
EntityAlpha blocker,0
HideEntity blocker

;-----
;New game setup
;-----
playerlives=2
playerscore=0
levelbonus=100
domspicked=0
levelnum=0

.loadlevel
;-----
;Load level data and set up level
;-----
;Load data for level
levelnum=1;levelnum+1
;Open file for reading
filein = ReadFile("level"+levelnum+".dmm")
;Read the number of dominoes and the forces used to push the final domino
domcount=ReadInt(filein)
domdist#=ReadFloat(filein)

;Create arrays for visible domino objects and their tokamak versions
Dim obj(domcount)
Dim tokobj(domcount)

Dim domspot(domcount)
;domstat keeps track of the status of each domino. 0=Not in play. 1=In play. 2=Being removed from play.
Dim domstatus(domcount)

Dim domalpha#(domcount)

;The dom array holds the x and z positions and the rotation for each domino. Item 0 and item domcount+1 are invisible.
Dim dom#(domcount+1,1,1)

;From the level data, feed all the domino positions into the dom() array
For x=0 To domcount+1
	For z=0 To 1
		dom#(x,z,0)=ReadFloat(filein)
	Next
Next

;Create the dummy 'spot' objects for each domino. These are the parents of each domino item.
;As objects 0 and domcount+1 in the dom array are for the invisible dominoes, they don't need a spot object
For i=1 To domcount
	domspot(i)=CreateFloor(2,2)
	PositionEntity domspot(i),dom#(i,0,0),0.01,dom#(i,1,0)
Next

;Close the level data file
CloseFile(filein)

;Calculate the force needed to push over the last block
forcex#=(dom(domcount,0,0)-dom(domcount+1,0,0))/domdist#
forcez#=(dom(domcount,1,0)-dom(domcount+1,1,0))/domdist#

;---
;Work out the background theme
;1 = car park
;2 = golf course
;3 = construction site
leveltype=levelnum
While leveltype > 3
	leveltype=leveltype-3
Wend

;Texture relevant objects
EntityTexture levelbackdrop,leveltex(leveltype)

;---

enemycount=0 ;The current number of enemies in the level

maxenemycount=3 ;The maxiumum number of enemies in the level - influenced by levelnum

bullytimecheck#=15000 ;Bully starts walking from same position 15 seconds after game starts
enemytimecheck#=5000 ;influenced by game difficulty - the period of time that the game checks whether new enemies are needed ingame

;--
;Create domino and tokamak objects for all visible dominos
;Domino objects are initially marked as inactive until the player moves over their assigned spot
For i=1 To domcount
	domalpha(i)=2

	obj(i) = CreateCube(domspot(i))
	tokobj(i)=TOKRB_Create()

	ScaleEntity obj(i),(domw#*domscale#)/2,(domh#*domscale#)/2,(domb#*domscale#)/2
	TOKRB_AddBox tokobj(i),domw#*domscale#,domh#*domscale#,domb#*domscale#

	;Average out the two rotations and set it as the rotation
	dom#(i,0,1)=((ATan2((dom#(i+1,0,0)-dom#(i,0,0)),(dom#(i,1,0)-dom#(i+1,1,0))))+(ATan2((dom#(i-1,0,0)-dom#(i,0,0)),(dom#(i,1,0)-dom#(i-1,1,0)))))/2
	
	TOKRB_SetLinearDamping tokobj(i),0.001
	TOKRB_SetAngularDamping tokobj(i),0.02
	TOKRB_SetMass tokobj(i),8
	TOKRB_SetBoxInertiaTensor tokobj(i),domw#,domh#,domb#,8
	
	;Move dominoes to assigned positions and rotate	
	TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
	
	RotateEntity domspot(i),0,dom#(i,0,1),0
	
	;Set all dominoes (except the last 'master' domino) as inactive and make the objects invisible
	If i<>1
		TOKRB_SetPosition tokobj(i),dom#(i,0,0),-20,dom#(i,1,0)
		TOKRB_GravityEnable tokobj(i),False
		HideEntity obj(i)
		domstatus(i)=0
	Else
		TOKRB_SetPosition tokobj(i),dom#(i,0,0),2,dom#(i,1,0)
		PositionEntity obj(i),TOKRB_GetX#(tokobj(i)),TOKRB_GetY#(tokobj(i)),TOKRB_GetZ#(tokobj(i)),True
		TOKRB_GravityEnable tokobj(i),True
		ShowEntity obj(i)
		domstatus(i)=1
	EndIf

	TOKRB_SetCollisionID(tokobj(i),2)

Next

;Calculate distances between clock and final domino
clockdist1#=(clockstartz+(playdown*-1))
clockdist2#=(clockstartx+EntityX(obj(1)))
If EntityZ(obj(1))>0
	clockdist3#=(EntityZ(obj(1))+(playdown*-1))
Else
	clockdist3#=((EntityZ(obj(1))*-1)+(playdown*-1))
EndIf

;Use this to calculate the speed of the clock (should take 70 seconds to reach master domino)
clockspeed#=(clockdist1#+clockdist2#+clockspeed3#)/3000


.levelintro
;----
;Pause before game begins and display level number
;----
period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

gametimer=MilliSecs()
gametimervar=0

While Not KeyHit(1)

	If gametimer+1000<=MilliSecs()
		gametimervar=gametimervar+1
		gametimer=MilliSecs()
	EndIf

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
		For k=1 To ticks
			time=time+period
			If k=ticks Then
				CaptureWorld
				
				;Fade in level number
				
				;Fade out blocker
				
				;Fade out level number
				
			EndIf
		Next

	CameraZoom cam,zoom#
	PointEntity cam,centerpoint

	RenderWorld tween
	
	Text 0,0,gametimervar
	
	Flip False

Wend


.levelstart
;Normal game runspeed = period=1000/FPS. Slower runspeed = period=2000/FPS
;Also need to change millisec times for spawning enemies to follow the slower speed
period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

maingametime#=MilliSecs() ;used to keep track of the game time
enemygametime#=MilliSecs() ;used to create new enemies every ... seconds

playerkey=0

;Put characters in starting positions and set starting variables
; Player
TOKRB_SetPosition(tokplayer,0,2.3,playup-2)
HideEntity fakedom
playerstung=0
; Bully
TOKRB_SetPosition(tokbully,0,4.7,playup+fadedistance)
PositionEntity bully,TOKRB_GetX#(tokbully),TOKRB_GetY#(tokbully),TOKRB_GetZ#(tokbully)
PositionEntity bullyshadow,TOKRB_GetX#(tokbully),0.1,TOKRB_GetZ#(tokbully)
TOKRB_GravityEnable tokbully,True
TOKRB_SetVelocity tokbully,0,0,0
EntityAlpha bully,0
EntityAlpha bullyshadow,0
HideEntity bully
HideEntity bullyshadow
bullyactive=0
bullyonce=0
bullydazeon=0 : bullydazetime=0

; Bee
ShowEntity bee : ShowEntity beeshadow
PositionEntity bee,0,beeheight,playdown-3-fadedistance
beeswat=0

; Money Bag
HideEntity money
moneyspawn=0
moneyspawntime#=MilliSecs()

;Clock
PositionEntity clock,clockstartx,clockstarty,clockstartz

;Other enemies
enemycount=0
enemyaction=0

For i=0 To 4
	If levelenemies(leveltype,i,1,0,0)<>0
		If levelenemies(leveltype,i,1,0,0)>6 Then levelenemies(leveltype,i,1,0,0) = levelenemies(leveltype,i,1,0,0)-6
		TOKRB_SetPosition(toklevelenemies(leveltype,i),0,-30,0)
		TOKRB_GravityEnable toklevelenemies(leveltype,i),False
		HideEntity levelenemies(leveltype,i,0,0,0)
		EntityAlpha levelenemies(leveltype,i,0,0,0),0
		HideEntity shadowenemies(i)
		EntityAlpha shadowenemies(i),0
		;Clear enemydaze array
		enemydaze(i,0)=0
		enemydaze(i,1)=0
	EndIf
Next

;---------------------
;MAIN GAME LOOP BEGINS
;---------------------

Repeat

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
	;Activate the bully after bully time has passed (only once per game)
	If bullyonce=0 ;ensures that this only happens once
		If maingametime+bullytimecheck<=MilliSecs()
			bullyactive=1
			TOKRB_SetPosition(tokbully,0,4.7,playup+fadedistance)
			TOKRB_GravityEnable tokbully,True
			ShowEntity bully
			ShowEntity bullyshadow
			EntityAlpha bully,0
			EntityAlpha bullyshadow,0
			bullyonce=1
		EndIf
	EndIf
	
	;Every certain amount of time, check And activate enemies
	If enemygametime+enemytimecheck<=MilliSecs()
		If enemycount<maxenemycount ;as long as they're aren't already enough enemies ingame
		
			For i=1 To 4 ;Loop through all enemies relevent to the level
				;As long as the enemy isn't empty (by checking type as 0) And enemy isn't already active (checked by making sure type is less than or equal to 7)
				If levelenemies(leveltype,i,1,0,0)<>0 And levelenemies(leveltype,i,1,0,0)<=7
		
					If levelenemies(leveltype,i,1,0,0)=1 ;If it's a vertically moving enemy
						;Position either at the top or the bottom of the screen and face into the screen  
						sidechoose=Rand(1,2) ;1 = top of screen, 2 = bottom of screen
						If sidechoose=1 Then sidechoose=playup+fadedistance : enemydir=-1 Else sidechoose=playdown-fadedistance : enemydir=1
						
						poschoose=Rand(EntityX#(obj(1),1)+3,playright-3)

						TOKRB_SetPosition(toklevelenemies(leveltype,i),poschoose,enemyheight(leveltype,i),sidechoose)
						PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))
						PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))
			
						levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*enemydir ;enemy gamespeed = enemyspeed * direction (set above)
						
						;Activate enemy
						levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)+6
						TOKRB_GravityEnable toklevelenemies(leveltype,i),True
						ShowEntity levelenemies(leveltype,i,0,0,0)
						ShowEntity shadowenemies(i)
						
						enemycount=enemycount+1

						Exit ;Exit the loop, so no more than 1 enemy is activated at a time
					ElseIf levelenemies(leveltype,i,1,0,0)=2 Or levelenemies(leveltype,i,1,0,0)=5  ;ElseIf it's a horizontally moving enemy
					
						;Position either at the top or the bottom of the screen and face into the screen  
						sidechoose=Rand(1,2) ;1 = left of screen, 2 = right of screen
						If sidechoose=1 Then sidechoose=playleft-fadedistance : enemydir=1 Else sidechoose=playright+fadedistance : enemydir=-1
						
						poschoose=Rand(playdown+3,playup-3)
						
						;If poschoose is going to send the enemy straight towards the domino
						If poschoose<=EntityZ#(obj(1),1)+4 And poschoose>=EntityZ#(obj(1),1)-4
						
							If EntityZ#(obj(1),1)>0 Then entz#=EntityZ#(obj(1),1)*-1 Else entz#=EntityZ#(obj(1),1)
							
							;If the space below the domino is greater than the space above the domino
							If playup-entz <= (playdown*-1)-entz
								poschoose=poschoose-4
							Else
								poschoose=poschoose+4
							EndIf
						
						EndIf
						
						TOKRB_SetPosition(toklevelenemies(leveltype,i),sidechoose,enemyheight(leveltype,i),poschoose)
						PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))
						PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))
		
						levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*enemydir ;enemy gamespeed = enemyspeed * direction (set above)
		
						;Activate enemy
						levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)+6
						TOKRB_GravityEnable toklevelenemies(leveltype,i),True
						ShowEntity levelenemies(leveltype,i,0,0,0)
						ShowEntity shadowenemies(i)
						
						enemycount=enemycount+1
						
						Exit ;Exit the loop, so no more than 1 enemy is activated at a time
					ElseIf levelenemies(leveltype,i,1,0,0)=3 Or levelenemies(leveltype,i,1,0,0)=6 ;Elseif it's a diagonally moving enemy
					
						;Position either at the top, bottom, right or left of the screen and face into the screen  
						sidechoose=Rand(1,4);(1,4) ;1 = left, 2 = right, 3 = top, 4 = bottom
						
						If sidechoose=1 ;if it's on the left, we should make sure it's not going to immeditatly hit the master domino.
							sidechoose=playleft-fadedistance
							poschoose=Rand(playdown+3,playup-3)
							;If poschoose is going to send the enemy straight towards the domino
							If poschoose<=EntityZ#(obj(1),1)+8 And poschoose>=EntityZ#(obj(1),1)-8
								If EntityZ#(obj(1),1)>0 Then entz#=EntityZ#(obj(1),1)*-1 Else entz#=EntityZ#(obj(1),1)
								;If the space below the domino is greater than the space above the domino
								If playup-entz <= (playdown*-1)-entz
									poschoose=poschoose-8
								Else
									poschoose=poschoose+8
								EndIf
							EndIf
							;set speed of enemy depending on enemy starting position and enemy maximum speed
							;diagdetails(leveltype,i,1,0)=x
							;diagdetails(leveltype,i,0,1)=z
							If poschoose>=-13 ;-13 is in the z centre of the playfield
								If poschoose<=EntityZ#(obj(1),1)+20
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
								Else
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
								EndIf
							ElseIf poschoose < -13
								If poschoose>=EntityZ#(obj(1),1)-20
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
								Else
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
								EndIf
							EndIf
							
							TOKRB_SetPosition(toklevelenemies(leveltype,i),sidechoose,enemyheight(leveltype,i),poschoose)
							PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))
							PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))
		
							;Activate enemy
							levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)+6
							TOKRB_GravityEnable toklevelenemies(leveltype,i),True
							ShowEntity levelenemies(leveltype,i,0,0,0)
							ShowEntity shadowenemies(i)
						
							enemycount=enemycount+1
							
							Exit ;Exit the loop, so no more than 1 enemy is activated at a time
						ElseIf sidechoose=2 ;right
							sidechoose=playright+fadedistance
							poschoose=Rand(playdown+3,playup-3)
							;set speed of enemy
							If poschoose>=-13 ;-13 is in the z centre of the playfield
								diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1 ;x
								diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
							ElseIf poschoose < -13
								diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1 ;x
								diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
							EndIf
							
							TOKRB_SetPosition(toklevelenemies(leveltype,i),sidechoose,enemyheight(leveltype,i),poschoose)
							PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))
							PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))
		
							;Activate enemy
							levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)+6
							TOKRB_GravityEnable toklevelenemies(leveltype,i),True
							ShowEntity levelenemies(leveltype,i,0,0,0)
							ShowEntity shadowenemies(i)
						
							enemycount=enemycount+1
							
							Exit ;Exit the loop, so no more than 1 enemy is activated at a time
						ElseIf sidechoose=3 ;top
							sidechoose=Rand(playleft+3,playright-3)
							poschoose=playup+fadedistance
							;set speed of enemy
							If sidechoose>=0
								diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1 ;x
								diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
							ElseIf sidechoose<0
								diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)
								diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
							EndIf
							
							TOKRB_SetPosition(toklevelenemies(leveltype,i),sidechoose,enemyheight(leveltype,i),poschoose)
							PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))
							PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))

							;Activate enemy
							levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)+6
							TOKRB_GravityEnable toklevelenemies(leveltype,i),True
							ShowEntity levelenemies(leveltype,i,0,0,0)
							ShowEntity shadowenemies(i)
						
							enemycount=enemycount+1
							
							Exit ;Exit the loop, so no more than 1 enemy is activated at a time
						ElseIf sidechoose=4 ;bottom
							sidechoose=Rand(playleft+3,playright-3)
							poschoose=playdown-fadedistance
							;set speed of enemy
							If sidechoose>=0
								diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1 ;x
								diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
							ElseIf sidechoose<0
								diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
								diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
							EndIf
							
							TOKRB_SetPosition(toklevelenemies(leveltype,i),sidechoose,enemyheight(leveltype,i),poschoose)
							PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))
							PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))
		
							;Activate enemy
							levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)+6
							TOKRB_GravityEnable toklevelenemies(leveltype,i),True
							ShowEntity levelenemies(leveltype,i,0,0,0)
							ShowEntity shadowenemies(i)
						
							enemycount=enemycount+1
							
							Exit ;Exit the loop, so no more than 1 enemy is activated at a time
						EndIf
						
					EndIf
				
				EndIf
			
			Next
		
		EndIf
		enemygametime=MilliSecs()
	EndIf
	
	
	;Hitting using the domino - only if the player is not moving (and player isn't already hitting something)
	If playerkey=1 And playermove=0 And playerhit<>1
		playerhit=1
		hittime#=MilliSecs()
	EndIf
		
	;Doing the actual hitting
	If playerhit=1
		If hittime#+50>MilliSecs()
			;Slight pause before 'animating' the hit domino
		ElseIf hittime+(hitpause/2)>=MilliSecs()
			;move fakedom into first hitting position
			RotateEntity fakedom,0,0,fakedomrot1#
			PositionEntity fakedom,fakedomx1#,fakedomy1#,0
			ShowEntity fakedom
		ElseIf hittime+(hitpause/2)<=MilliSecs() And hittime+hitpause>=MilliSecs()
			;move fakedom into second hitting position
			RotateEntity fakedom,0,0,fakedomrot2#
			PositionEntity fakedom,fakedomx2#,fakedomy2#,0
			donehit=1
		Else
			;stop hitting
			playerhit=0
			donehit=0
			RotateEntity fakedom,0,0,fakedomrot1#
			PositionEntity fakedom,fakedomx1#,fakedomy1#,0
			HideEntity fakedom
		EndIf
		If playermove=1 ;if the playermoves in the middle of a hit
			TOKRB_SetPosition(tokplayer,EntityX(player),EntityY(player),EntityZ(player)) ;keep him still
		EndIf
	EndIf

	playerx#=EntityX#(player)
	playerz#=EntityZ#(player)
	
	domstanding=0
	
	;Looping through every visible domino
	For i=1 To domcount
	
		If domstatus(i)=1 Then domstanding=domstanding+domstatus(i)
	
		;If the domino has fallen over (or as close as can be told)
		If EntityRoll(obj(i)) > 40 Or EntityRoll(obj(i)) < -40 ;and we want it to disappear
			;Mark it as needing removing from play
			domstatus(i)=2
		EndIf
		
		;If player is near to a domino spot...
		domspotx#=EntityX#(domspot(i))
		domspotz#=EntityZ#(domspot(i))
				
		If playerx# < domspotx#+domspx# And playerx# > domspotx#-domspx# And playerz# < domspotz#+domspz# And playerz# > domspotz#-domspz#
				;If the playerkey is not being held down...
				If playerkey=0 And playerhit=0
					;If the domino is not in play
					;Play animation and sound of player righting domino (we don't want this To happen every time - the domino may have already been righted, but now just needs straightening)
					;If domstatus(i)<>1
					;Endif
				
					TOKRB_GravityEnable tokobj(i),True

					TOKRB_SetPosition tokobj(i),dom#(i,0,0),2,dom#(i,1,0)
					TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
					TOKRB_SetVelocity tokobj(i),0,0,0
				
					domalpha(i)=2
					EntityAlpha obj(i),domalpha(i)
					ShowEntity obj(i)
					
					;Add points as long as the domino isn't already standing up
					If domstatus(i)<>1
						playerscore=playerscore+100
						domspicked=domspicked+1
					EndIf
					
					domstatus(i)=1
					
				;Otherwise, if the player key is being held down...
				ElseIf playerkey=1
						TOKRB_SetPosition tokobj(i),dom#(i,0,0),-20,dom#(i,1,0)
						TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
						TOKRB_SetVelocity tokobj(i),0,0,0
						TOKRB_GravityEnable tokobj(i),False
						domstatus(i)=0
				EndIf
		EndIf
		
	Next


	For k=1 To ticks
		time=time+period
		If k=ticks Then
			CaptureWorld
			
			bullyx#=TOKRB_GetX#(tokbully)
			bullyz#=TOKRB_GetZ#(tokbully)
			
			For i=1 To domcount
				toklocx#=TOKRB_GetX#(tokobj(i))
				toklocz#=TOKRB_GetZ#(tokobj(i))
				
				;Position the tok dominoes
				PositionEntity obj(i),toklocx#,TOKRB_GetY#(tokobj(i)),toklocz#,True
				RotateEntity obj(i),TOKRB_GetPitch#(tokobj(i)),TOKRB_GetYaw#(tokobj(i)),TOKRB_GetRoll#(tokobj(i)),True
				;fade the dominoes
				If domstatus(i)=2
					If domalpha(i)>0
						domalpha(i)=domalpha(i)-domfadespeed#
						EntityAlpha obj(i),domalpha(i)
					Else
						;HideEntity obj(i)
						TOKRB_SetPosition tokobj(i),dom#(i,0,0),-20,dom#(i,1,0)
						TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
						TOKRB_SetVelocity tokobj(i),0,0,0
						TOKRB_GravityEnable tokobj(i),False
						domstatus(i)=0
					EndIf
				EndIf
				
				;If dominoes are near to the bully, kick them
				If bullyx# < toklocx#+bullyspx# And bullyx# > toklocx#-bullyspx# And bullyz# < toklocz#+bullyspz# And bullyz# > toklocz#-bullyspz#
					bullyforcex#=(dom(i,0,0)-bullyx#)
					bullyforcez#=(dom(i,1,0)-bullyz#)
					TOKRB_ApplyImpulse2 tokobj(i),bullyforcex#*8,10,bulyforcez#*8,toklocx#,TOKRB_GetY#(tokobj(i))+1,toklocz#
				EndIf
				
				;Check for the rest of the enemies
				For j = 1 To enemycount
					If TOKRB_GetX#(toklevelenemies(leveltype,j)) < toklocx#+levelenemies(leveltype,j,1,1,0) And TOKRB_GetX#(toklevelenemies(leveltype,j)) > toklocx#-levelenemies(leveltype,j,1,1,0) And TOKRB_GetZ#(toklevelenemies(leveltype,j)) < toklocz#+levelenemies(leveltype,j,1,2,0) And TOKRB_GetZ#(toklevelenemies(leveltype,j)) > toklocz#-levelenemies(leveltype,j,1,2,0)
						TOKRB_ApplyImpulse2 tokobj(i),(dom(i,0,0)-TOKRB_GetX#(toklevelenemies(leveltype,j))),0,(dom(i,1,0)-TOKRB_GetZ#(toklevelenemies(leveltype,j))),toklocx#,TOKRB_GetY#(tokobj(i))+2,toklocz#
					EndIf
				Next

			Next
			
			;Position the player
			PositionEntity player,TOKRB_GetX#(tokplayer),2.3,TOKRB_GetZ#(tokplayer)
			PositionEntity playershadow,TOKRB_GetX#(tokplayer),0.1,TOKRB_GetZ#(tokplayer)
			
			;Position the bully
			PositionEntity bully,TOKRB_GetX#(tokbully),4.7,TOKRB_GetZ#(tokbully)
			PositionEntity bullyshadow,TOKRB_GetX#(tokbully),0.1,TOKRB_GetZ#(tokbully)
			
			;Position the bee (actually done in the same place as the bee moving routine - no point doing it here)
			
			;Other enemies
			For i=1 To 4
				;As long as the enemy exists (has a type) and the enemy isn't dazed
				If levelenemies(leveltype,i,1,0,0)<>0
					PositionEntity levelenemies(leveltype,i,0,0,0),TOKRB_GetX#(toklevelenemies(leveltype,i)),enemyheight(leveltype,i),TOKRB_GetZ#(toklevelenemies(leveltype,i))+0.3 ;position is increased by 0.3 in z, just to give a slightly better impression
					PositionEntity shadowenemies(i),TOKRB_GetX#(toklevelenemies(leveltype,i)),0.1,TOKRB_GetZ#(toklevelenemies(leveltype,i))+0.3
				EndIf
			Next
				
		EndIf
		
		;Player keypresses and moving tok objects
		playermove=0
		
		If controlscheme=1 ;If keyboard control is selected
			If KeyDown(200) Then TOKRB_ApplyImpulse tokplayer,0,0,(playerspeedz#) : playermove=1 ;Up
			If KeyDown(208) Then TOKRB_ApplyImpulse tokplayer,0,0,(playerspeedz#)*-1 : playermove=1 ;Down
			If KeyDown(203) Then TOKRB_ApplyImpulse tokplayer,(playerspeedx#)*-1,0,0 :playermove=1 ;Left
			If KeyDown(205) Then TOKRB_ApplyImpulse tokplayer,(playerspeedx#),0,0 : playermove=1 ;Right
			;Space can also be used, but may cause problems when holding it down, due to conflicts in many keyboard's matrices
			If KeyDown(29) Or KeyDown(57) Then playerkey=1 Else playerkey=0 ;Action Key
		ElseIf controlscheme=2 ;If mouse control is selected
			;Position the mousetarget below the pointer
			;Translate screen X and Y into playfield X and Z
			PositionEntity playermousetarget,(MouseX()-(GraphicsWidth()/2))/11,0,((MouseY()-(GraphicsHeight()/2))/-5.8)-2
			;As long as the player isn't already close enough to the mouse target
			;Move the player towards the mouse target
			playerforcex#=(EntityX#(playermousetarget)-(EntityX#(player)))/EntityDistance(player,playermousetarget)
			playerforcez#=(EntityZ#(playermousetarget)-(EntityZ#(player)))/EntityDistance(player,playermousetarget)
			TOKRB_ApplyImpulse tokplayer,playerforcex#*playerspeedx#,0,playerforcez#*playerspeedz# : playermove=1
			;Action Key
			If MouseDown(1) Then playerkey=1 Else playerkey=0
		EndIf

		;Test key for toppling the middle domino
		;If KeyHit(57) Then
		;	dompush=Int(domcount/2)
		;	newforcex#=(dom(dompush,0,0)-dom(dompush+1,0,0))/domdist#
		;	newforcez#=(dom(dompush,1,0)-dom(dompush+1,1,0))/domdist#
		;	TOKRB_ApplyImpulse2 tokobj(dompush),newforcex*40,0,newforcez*40,TOKRB_GetX#(tokobj(dompush)),TOKRB_GetY#(tokobj(dompush))+2,TOKRB_GetZ#(tokobj(dompush))
		;EndIf
		
		;For pushing the bully faster
		temptokplayx#=TOKRB_GetX(tokplayer) : temptokbullx#=TOKRB_GetX(tokbully) : temptokplayz#=TOKRB_GetZ(tokplayer) : temptokbullz#=TOKRB_GetZ(tokbully)
		;If the velocity is greater than it'd usually be, and the player is behind the bully
		If TOKRB_GetVelocityZ(tokbully)<-1 And temptokplayx# > temptokbullx#-2 And temptokplayx# < temptokbullx#+2 And temptokplayz# < temptokbullz#+4.1 And temptokplayz# > temptokbullz#
			bullyspeed#=-4
		Else
			bullyspeed#=-1
		EndIf
		
		;Move bully (as long as he's in the playfield)
		If bullyactive=1
			If bullydazeon=0 ;and if the bully isn't dazed
				If TOKRB_GetZ#(tokbully)>playdown-fadedistance#
					TOKRB_SetVelocity tokbully,0,0,bullyspeed#
				Else ;deactivate bully
					;Deactivate enemy
					TOKRB_SetVelocity tokbully,0,0,0
					TOKRB_SetPosition(tokbully,-30,-30,-100)
					TOKRB_GravityEnable tokbully,False
					EntityAlpha bully,0
					EntityAlpha bullyshadow,0
					HideEntity bully
					HideEntity bullyshadow
					bullyactive=0
				EndIf
			Else
				TOKRB_SetVelocity tokbully,0,0,0
			EndIf
			;Fade the bully
			bullyfadepos#=0
			If TOKRB_GetZ(tokbully) > playup
				bullyfadepos#=TOKRB_GetZ(tokbully)-playup
			EndIf
			If TOKRB_GetZ(tokbully) < playdown
				bullyfadepos#=playdown-TOKRB_GetZ(tokbully)
			EndIf
			EntityAlpha bully,1-(1/(fadedistance#/bullyfadepos))
			EntityAlpha bullyshadow,1-(1/(fadedistance#/bullyfadepos))
		EndIf
		
		;Checking for the bully being hit by the player's hitdomino
		If donehit=1
			If TOKRB_GetX#(tokbully) < EntityX#(fakedom,1)+hitsizex# And TOKRB_GetX#(tokbully) > EntityX#(fakedom,1)-hitsizex# And TOKRB_GetZ#(tokbully) < EntityZ#(fakedom,1)+hitsizez# And TOKRB_GetZ#(tokbully) > EntityZ#(fakedom,1)-hitsizez#
				If bullydazeon=0 ;as long as the enemy isn't already dazed
					bullydazeon=1
					bullydazetime=MilliSecs()
				EndIf
			EndIf
		EndIf
				
		;Working bully daze
		If bullydazeon=1
			If bullydazetime+enemydazepause<=MilliSecs()
				;'undaze' the bully
				bullydazeon=0
				bullydazetime=0
			EndIf
		EndIf
				
		;Checking for the bee being hit by the player's hitdomino
		If donehit=1
			If EntityX#(bee) < EntityX#(fakedom,1)+hitsizex# And EntityX#(bee) > EntityX#(fakedom,1)-hitsizex# And EntityZ#(bee) < EntityZ#(fakedom,1)+hitsizez# And EntityZ#(bee) > EntityZ#(fakedom,1)-hitsizez#
				If beeswat=0 ;as long as the enemy isn't already dazed
					beeswat=1
					HideEntity bee
					HideEntity beeshadow
				EndIf
			EndIf
		EndIf
		
		;Move bee + beeshadow
		If beeswat=0 ;if the bee is alive
			If EntityX#(player) < EntityX#(bee)+beestingsizex# And EntityX#(player) > EntityX#(bee)-beestingsizex# And EntityZ#(player) < EntityZ#(bee)+beestingsizez# And EntityZ#(player) > EntityZ#(bee)-beestingsizez#
				playerstung=1
			Else
				PointEntity bee,player
				MoveEntity bee,0,0,beespeed
				PositionEntity bee,EntityX#(bee),beeheight,EntityZ#(bee)
			EndIf
		EndIf
		PositionEntity beeshadow,EntityX#(bee),0.1,EntityZ#(bee)
		
		If beeswat=1
			If moneyspawn=0
				;Randomly position the money somewhere
				PositionEntity money,Rand(-20,20),moneyheight#,EntityZ#(bee)+2 ;Position in a random position somewhere in the play field, but near to where the bee was swatted
				ShowEntity money
				;Reset bee position
				PositionEntity bee,0,beeheight,playdown-3-fadedistance
				PositionEntity beeshadow,EntityX#(bee),0.1,EntityZ#(bee)
				moneyspawntime#=MilliSecs()
				moneyspawn=1
			EndIf
		EndIf
		
		If moneyspawn=1 ;If a sack of money has been created
			;If the player is near to the money, collect and delete it and re-start the bee
			If EntityX#(player) < EntityX#(money)+moneypicksizex# And EntityX#(player) > EntityX#(money)-moneypicksizex# And EntityZ#(player) < EntityZ#(money)+moneypicksizez# And EntityZ#(player) > EntityZ#(money)-moneypicksizez#
				playerscore=playerscore+moneyvalue
				HideEntity money
				moneyspawn=0
				moneyspawntime#=0
				beeswat=0

			EndIf
			;Or, if the player doesn't collect the money in a certain time, do the same (but no points)
			If moneyspawntime+enemydazepause<=MilliSecs()
				HideEntity money
				moneyspawn=0
				moneyspawntime#=0
				beeswat=0
			EndIf
			ShowEntity bee
			ShowEntity beeshadow
		EndIf
		
		
		;Move the clock
		If EntityZ#(clock) >= playdown+3 And EntityX#(clock) = clockstartx
			MoveEntity clock,0,0,clockspeed#*-1
		ElseIf EntityZ#(clock) <= playdown+3 And EntityX#(clock) >= EntityX#(obj(1),1)
			MoveEntity clock,clockspeed#*-1,0,0
		EndIf
		
		;Giving enemiess who pause the chance (this assumes only 1 changing enemy per level, or they'd all stop at the same time)
		If Rand(1,500)=1 And enemyaction=0
			enemyaction=1
			enemyactiontime#=MilliSecs()
		EndIf
		If enemyaction=1
			If enemyactiontime#+enemywaittime#<=MilliSecs()
				enemyaction=0
			EndIf
		EndIf
		;The same for enemies who change direction (although this occurs more often than the pause)
		If Rand(1,enemydirectionchange)=1
			enemychangedir=1
		Else
			enemychangedir=0
		EndIf
		
		;Move other enemies, depending on type
		;Loop through every active enemy
		For i=1 To 4
		
			If levelenemies(leveltype,i,1,0,0)>6 ;As long as the enemy is active (enemy type greater than 6)
		
				If enemydaze(i,0)<>1 ;Only move them if they are not dazed and not pausing
					
					If enemyaction=1 And levelenemies(leveltype,i,1,0,0)=11 ;If the horizontal type is active
						;Do nothing (pause enemy movement)
						;Change animation frame to enemy resting / pausing
					Else
						;Routine to change enemy direction if they are being pushed at a certain velocity (switchspeed)
						If levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1) ;If gamespeed = enemyspeed (if the enemy is moving in a positive direction)
							If levelenemies(leveltype,i,1,0,0)=7 ;If it's a vertically moving enemy
								If TOKRB_GetVelocityZ(toklevelenemies(leveltype,i))<(levelenemies(leveltype,i,1,0,2)*-1) ;if velocity is less than switchspeed * -1
									levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*-1 ;set gamespeed as enemyspeed *-1
								EndIf
							ElseIf levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 ;ElseIf it's a horizontally moving enemy
								If TOKRB_GetVelocityX(toklevelenemies(leveltype,i))<(levelenemies(leveltype,i,1,0,2)*-1)
									levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*-1 ;set gamespeed as enemyspeed *-1
								EndIf
							EndIf
						ElseIf levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*-1 ;If gamespeed = enemyspeed * -1 (if enemy is moving in a negative direction)
							If levelenemies(leveltype,i,1,0,0)=7 ;If it's a vertically moving enemy
								If TOKRB_GetVelocityZ(toklevelenemies(leveltype,i))>levelenemies(leveltype,i,1,0,2) ;If velocity is more than switchspeed
									levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1) ;set gamespeed as enemyspeed
								EndIf
							ElseIf levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 ;ElseIf it's a horizontally moving enemy
								If TOKRB_GetVelocityX(toklevelenemies(leveltype,i))>levelenemies(leveltype,i,1,0,2)
									levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1) ;set gamespeed as enemyspeed
								EndIf
							EndIf
						EndIf
						;Changing directions when pushed for diagonal enemies (not the same as horizontal and vertical)
						If levelenemies(leveltype,i,1,0,0)=9 Or levelenemies(leveltype,i,1,0,0)=12
						;X
							If diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)
								If TOKRB_GetVelocityX#(toklevelenemies(leveltype,i))<-0.1
									;push him in a negative x direction
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1 ;x
								EndIf
								If TOKRB_GetVelocityX#(toklevelenemies(leveltype,i))>2
									;push him in a positive x direction
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
								EndIf
							ElseIf diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1
								If TOKRB_GetVelocityX#(toklevelenemies(leveltype,i))>0.1
									;push him in a positive x direction
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1) ;x
								EndIf
								If TOKRB_GetVelocityX#(toklevelenemies(leveltype,i))<-2
									;push him in a negative x direction
									diagdetails(leveltype,i,1,0)=levelenemies(leveltype,i,1,0,1)*-1 ;x
								EndIf
							EndIf
						;Z
							If diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)
								If TOKRB_GetVelocityZ#(toklevelenemies(leveltype,i))<-0.1
									;push him in a negative z direction
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
								EndIf
								If TOKRB_GetVelocityZ#(toklevelenemies(leveltype,i))>2
									;push him in a positive z direction
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
								EndIf
							ElseIf diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1
								If TOKRB_GetVelocityZ#(toklevelenemies(leveltype,i))>0.1
									;push him in a positive x direction
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1) ;z
								EndIf
								If TOKRB_GetVelocityZ#(toklevelenemies(leveltype,i))<-2
									;push him in a negative x direction
									diagdetails(leveltype,i,0,1)=levelenemies(leveltype,i,1,0,1)*-1 ;z
								EndIf
							EndIf
					
						EndIf
				
						;Routine to move enemy - as long as they are not being pushed in a direction opposite to their type
						If levelenemies(leveltype,i,1,0,0)=7 ;If it's a vertically moving enemy
		
							;If pushing horizontally, stop any vertical motion
							If TOKRB_GetVelocityX(toklevelenemies(leveltype,i))<(levelenemies(leveltype,i,1,0,2)*-1) Or TOKRB_GetVelocityX(toklevelenemies(leveltype,i))>levelenemies(leveltype,i,1,0,2)
								;Do nothing
							Else
								TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,3) ;Move enemy by gamespeed
							EndIf
							
			
						ElseIf levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 ;ElseIf it's a horizontally moving enemy
		
							;If pushing vertically, stop any horizontal motion
							If TOKRB_GetVelocityZ(toklevelenemies(leveltype,i))<(levelenemies(leveltype,i,1,0,2)*-1) Or TOKRB_GetVelocityZ(toklevelenemies(leveltype,i))>levelenemies(leveltype,i,1,0,2)
								;Do nothing
							Else
								TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,3),0,0
							EndIf
		
						ElseIf levelenemies(leveltype,i,1,0,0)=9 Or levelenemies(leveltype,i,1,0,0)=12 ;Elseif it's a diagonally moving enemy

							TOKRB_SetVelocity toklevelenemies(leveltype,i),diagdetails(leveltype,i,1,0),0,diagdetails(leveltype,i,0,1)

						EndIf
						
					EndIf
			
				EndIf;If enemydaze=0
			
				;Fading and removing enemies from the game. Game also moves them in the direction of the edge once they pass out of the play area
			
				enemyfadepos#=0
			
				If TOKRB_GetZ(toklevelenemies(leveltype,i)) < playdown ;If enemy goes below playfield
					If levelenemies(leveltype,i,1,0,0)=7 And enemydaze(i,0)=1 ;a vertical enemy in dazed state
						levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*-1
						TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,3)
					EndIf
					If levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 ;a horizontal enemy
						TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,1)*-1
					EndIf
					If levelenemies(leveltype,i,1,0,0)=9 Or levelenemies(leveltype,i,1,0,0)=12 And enemydaze(i,0)=1 ;a diagonal enemy in dazed
						diagdetails(leveltype,i,1,0)=0
						TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,1)*-1
					EndIf
					enemyfadepos#=playdown-TOKRB_GetZ(toklevelenemies(leveltype,i))
				ElseIf TOKRB_GetZ(toklevelenemies(leveltype,i)) > playup ;If enemy goes above playfield
					If levelenemies(leveltype,i,1,0,0)=7 And enemydaze(i,0)=1 ;a vertical enemy in dazed state
						levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)
						TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,3)
					EndIf
					If levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 ;a horizontal enemy
						TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,1)
					EndIf
					If levelenemies(leveltype,i,1,0,0)=9 Or levelenemies(leveltype,i,1,0,0)=12 And enemydaze(i,0)=1 ;a diagonal enemy in dazed
						diagdetails(leveltype,i,1,0)=0
						TOKRB_SetVelocity toklevelenemies(leveltype,i),0,0,levelenemies(leveltype,i,1,0,1)
					EndIf
					enemyfadepos#=TOKRB_GetZ(toklevelenemies(leveltype,i))-playup
				ElseIf TOKRB_GetX(toklevelenemies(leveltype,i)) < playleft ;If enemy goes to left of playfield
					If levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 And enemydaze(i,0)=1 ;a horizontal enemy in dazed state
						levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)*-1
						TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,3),0,0
					EndIf
					If levelenemies(leveltype,i,1,0,0)=7 ;a vertical enemy
						TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1)*-1,0,0;levelenemies(leveltype,i,1,0,3)
					EndIf
					If levelenemies(leveltype,i,1,0,0)=9 Or levelenemies(leveltype,i,1,0,0)=12 And enemydaze(i,0)=1 ;a diagonal enemy in dazed
						diagdetails(leveltype,i,0,1)=0
						TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1)*-1,0,0
					EndIf
					enemyfadepos#=playleft-TOKRB_GetX(toklevelenemies(leveltype,i))
				ElseIf TOKRB_GetX(toklevelenemies(leveltype,i)) > playright ;If enemy goes to right of playfield
					If levelenemies(leveltype,i,1,0,0)=8 Or levelenemies(leveltype,i,1,0,0)=11 And enemydaze(i,0)=1 ;a horizontal enemy in dazed state
						levelenemies(leveltype,i,1,0,3)=levelenemies(leveltype,i,1,0,1)
						TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,3),0,0
					EndIf
					If levelenemies(leveltype,i,1,0,0)=7 ;a vertical enemy
						TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1),0,0;levelenemies(leveltype,i,1,0,3)
					EndIf
					If levelenemies(leveltype,i,1,0,0)=9 Or levelenemies(leveltype,i,1,0,0)=12 And enemydaze(i,0)=1 ;a diagonal enemy in dazed
						diagdetails(leveltype,i,0,1)=0
						TOKRB_SetVelocity toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1),0,0
					EndIf
					enemyfadepos#=TOKRB_GetX(toklevelenemies(leveltype,i))-playright
				EndIf
			
				EntityAlpha levelenemies(leveltype,i,0,0,0),1-(1/(fadedistance#/enemyfadepos))
				EntityAlpha shadowenemies(i),1-(1/(fadedistance#/enemyfadepos))
			
				If enemyfadepos# > 10
					;Deactivate enemy
					TOKRB_SetPosition(toklevelenemies(leveltype,i),-100,-100,-100)
					TOKRB_GravityEnable toklevelenemies(leveltype,i),False
					HideEntity levelenemies(leveltype,i,0,0,0)
					levelenemies(leveltype,i,1,0,0)=levelenemies(leveltype,i,1,0,0)-6

					enemycount=enemycount-1
				EndIf
			
				;Checking For enemies being hit by the player's hitdomino
				If donehit=1
					If TOKRB_GetX#(toklevelenemies(leveltype,i)) < EntityX#(fakedom,1)+hitsizex# And TOKRB_GetX#(toklevelenemies(leveltype,i)) > EntityX#(fakedom,1)-hitsizex# And TOKRB_GetZ#(toklevelenemies(leveltype,i)) < EntityZ#(fakedom,1)+hitsizez# And TOKRB_GetZ#(toklevelenemies(leveltype,i)) > EntityZ#(fakedom,1)-hitsizez#
						If enemydaze(i,0)=0 ;as long as the enemy isn't already dazed
							enemydaze(i,0)=1
							enemydaze(i,1)=MilliSecs()
						EndIf
					EndIf
				EndIf
				
				;Working enemy daze
				If enemydaze(i,0)=1
					If enemydaze(i,1)+enemydazepause<=MilliSecs()
						;'undaze' enemy
						enemydaze(i,0)=0
						enemydaze(i,1)=0
					EndIf
				EndIf
				
				;Enemy 'AI', changing direction of enemy
				If levelenemies(leveltype,i,1,0,0)=12 And enemychangedir=1 And enemydaze(i,0)=0
					;As long as the enemy is in the playfield
					If TOKRB_GetX(toklevelenemies(leveltype,i))<playright-8 And TOKRB_GetX(toklevelenemies(leveltype,i))>playleft+8 And TOKRB_GetZ(toklevelenemies(leveltype,i))<playup-10 And TOKRB_GetZ(toklevelenemies(leveltype,i))>playdown+10
						;Random directions
						randnum=Rand(1,4)
						If randnum=1
							TOKRB_SetVelocity(toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1),0,levelenemies(leveltype,i,1,0,1))
						ElseIf randnum=2
							TOKRB_SetVelocity(toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1),0,levelenemies(leveltype,i,1,0,1)*-1)
						ElseIf randnum=3
							TOKRB_SetVelocity(toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1)*-1,0,levelenemies(leveltype,i,1,0,1))
						ElseIf randnum=4
							TOKRB_SetVelocity(toklevelenemies(leveltype,i),levelenemies(leveltype,i,1,0,1)*-1,0,levelenemies(leveltype,i,1,0,1)*-1)
						EndIf
						enemychangedir=0
					EndIf
				EndIf
			
			EndIf
			
			
		Next

		;Checks for game ending situations
		;If all of the game dominoes are standing upright
		If domstanding=domcount
			Goto levelcomplete
		EndIf
		;If first domino has been knocked over, or picked up
		If domstatus(1)<>1
			Goto playerlose
		EndIf
		;If player has been stung by bee
		If playerstung=1
			Goto playerlose
		EndIf
		
		TOKSIM_Advance(2.3/FPS,2)
		UpdateWorld
 	Next

	;Temp camera controls
	If KeyDown(72) Then MoveEntity cam,0,0.01,0 ;Up
	If KeyDown(80) Then MoveEntity cam,0,-0.01,0 ;Down
	If KeyDown(73) Then MoveEntity cam,0,0,0.01 ;In
	If KeyDown(81) Then MoveEntity cam,0,0,-0.01 ;Out
	If KeyDown(78) Then zoom#=zoom#+0.0001 ;Zoom +
	If KeyDown(74) Then zoom#=zoom#-0.0001 ;Zoom -
	
	;Pause (comes last so, hopefully, any other actions will have been carried out already)
	If KeyHit(1) Or KeyHit(25)
		Gosub pausegame
	EndIf
	
	CameraZoom cam,zoom#
	PointEntity cam,centerpoint
 
	RenderWorld tween
	
	txt(10,10,"Score "+playerscore,0)
	
	Text 0,30,"CamX " + EntityX(cam)
	Text 0,40,"CamY " + EntityY(cam)
	Text 0,50,"CamZ " + EntityZ(cam)
	Text 0,60,"Zoom " + zoom#

	Text 0,70,"Dominoes standing= "+domstanding

	Text 0,80,"Lives= "+playerlives
	Text 0,90,"Bonus= "+levelbonus
	
	Text 0,110,"Doms Picked= "+domspicked
	
	Text 0,130,"TESTVAR= "+test
	
	Text 0,150,"Enemy Count= "+enemycount
	
	Text 0,160,"Enemyfadepos= "+EntityX#(obj(1),1)
	
	Text 0,180,"Clockdist1= "+clockdist1
	Text 0,190,"Clockdist2= "+clockdist2
	Text 0,200,"Clockdist3= "+clockdist3
	Text 0,210,"Clockspeed= "+clockspeed
	
	Text 0,230,"X " + levelenemies(1,1,1,0,2)
	Text 0,240,"PX " + TOKRB_GetZ(tokplayer) + " BX " + TOKRB_GetZ(tokbully)
	
	;Drawing Mouse target on screen if mouse control is selected
	If controlscheme=2
		DrawImage mouseimage,MouseX(),MouseY()
	EndIf
	
	Flip False
	
Forever
;---------------------
;END OF MAIN GAME LOOP
;---------------------

.pausegame
;----
;When the game is paused, show a menu with the option to either return to the game, go to the options screen, or exit the game
;----
period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

EntityAlpha blocker,0
HideEntity blocker

;Record the reading of all timers used
oldmaingametime#=MilliSecs()-maingametime#
oldenemygametime#=MilliSecs()-enemygametime#
oldmoneyspawntime#=MilliSecs()-moneyspawntime#

;Record position of mouse (if mouse control is being used)

Dim menutext$(2)
Dim menuoption(2,1)

menutext(0)="Return to game"
menuoption(0,0)=250
menuoption(0,1)=1
menutext(1)="Options"
menuoption(1,0)=300
menuoption(1,1)=0
menutext(2)="Exit Game"
menuoption(2,0)=350
menuoption(2,1)=0

MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(0,0)+10

FlushKeys : FlushMouse

Repeat

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
	;Cursor Control for menu (using keyhit rather than keydown)
	If KeyHit(200) And KeyDown(208)<>1 ;Up
		;Depending on what is currently being selected
		If menuoption(0,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(2,0)+10 ;Start game
		If menuoption(1,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(0,0)+10 ;Options
		If menuoption(2,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(1,0)+10 ;Exit Game
	EndIf
	If KeyHit(208) And KeyDown(200)<>1 ;Down
		;Depending on what is currently being selected
		If menuoption(0,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(1,0)+10 ;Start game
		If menuoption(1,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(2,0)+10 ;Options
		If menuoption(2,1)=1 Then MoveMouse (GraphicsWidth()/2)+((Len(menutext(0))*26)/2)+20,menuoption(0,0)+10 ;Exit Game
	EndIf
	
	For i=0 To 2
		;Mouse Control for menu
		If MouseX()>((GraphicsWidth()/2)-((Len(menutext(0))*26)/2))-30 And MouseX()<((GraphicsWidth()/2)+((Len(menutext(0))*26)/2))+30 And MouseY()>menuoption(i,0) And MouseY()<menuoption(i,0)+30
			menuoption(i,1)=1
			;Turn off the other two options
			If i=0 Then menuoption(1,1)=0 : menuoption(2,1)=0
			If i=1 Then menuoption(0,1)=0 : menuoption(2,1)=0
			If i=2 Then menuoption(0,1)=0 : menuoption(1,1)=0
		EndIf
	Next
	
	;Menu item selection
	;Keyboard Control (pressing either Enter, Ctrl or Space selects items)
	If KeyDown(200)<>1 And KeyDown(208)<>1 And KeyDown(203)<>1 And KeyDown(205)<>1 ;making sure no other keys are being pressed
		;Pressing ESC
		If KeyHit(1) Or KeyHit(25) Then Exit ;Esc Or P restarts game straight away
					
		If KeyHit(28) Or KeyHit(29)  Or KeyHit(57)
			If menuoption(0,1)=1 Then Exit
			If menuoption(1,1)=1 Then optioncamefrom$="game" : Goto optionmenu
			If menuoption(2,1)=1 Then Gosub cleanlevel : Goto mainmenu
		EndIf

		;Mouse Control (left clicking selects items)
		If MouseHit(1)
			If menuoption(0,1)=1 Then Exit
			If menuoption(1,1)=1 Then optioncamefrom$="game" : Goto optionmenu
			If menuoption(2,1)=1 Then Gosub cleanlevel : Goto mainmenu
		EndIf
	EndIf
	
	

	CameraZoom cam,zoom#
	PointEntity cam,centerpoint

	RenderWorld tween
	
	menutxt(0,170,"Paused",0)
	For i= 0 To 2
		menutxt(0,menuoption(i,0),menutext(i),menuoption(i,1))
	Next
	
	DrawImage mouseimage,MouseX(),MouseY()
	
	Flip False

Forever

FlushKeys : FlushMouse

;Restart the time settings
time=MilliSecs()-period
imptime=MilliSecs()

maingametime#=MilliSecs()-oldmaingametime#
enemygametime#=MilliSecs()-oldenemygametime#
moneyspawntime#=MilliSecs()-oldmoneyspawntime#

;Move Mouse back to original position (if mouse control is being used)

Return ;Go back to wherever the pause command was called from

.playerlose
;----
;The player loses a life (sits on the floor) and then the level restarts (if he has a spare life) or ends
;----
period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

gametimer=MilliSecs()
timeofpause=3000

playerlives=playerlives-1
	
If playerlives>=0 Then
	;display 'Bonus reset to 100' message in main loop
	levelbonus=100
EndIf
	
While gametimer+timeofpause>=MilliSecs()

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
		For k=1 To ticks
			time=time+period
			If k=ticks Then
				CaptureWorld
				
				For i=1 To domcount
					;Position the tok dominoes
					PositionEntity obj(i),TOKRB_GetX#(tokobj(i)),TOKRB_GetY#(tokobj(i)),TOKRB_GetZ#(tokobj(i)),True
					RotateEntity obj(i),TOKRB_GetPitch#(tokobj(i)),TOKRB_GetYaw#(tokobj(i)),TOKRB_GetRoll#(tokobj(i)),True
					
					;Make sure no more dominoes can be knocked over
					If domstatus(i)=1
						TOKRB_SetPosition tokobj(i),dom#(i,0,0),2,dom#(i,1,0)
						TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
						TOKRB_SetVelocity tokobj(i),0,0,0
					EndIf
					
					;Fade away dominoes that have already fallen over
					If domstatus(i)=2
						If domalpha(i)>0
							domalpha(i)=domalpha(i)-domfadespeed#
							EntityAlpha obj(i),domalpha(i)
						Else
							;HideEntity obj(i)
							TOKRB_SetPosition tokobj(i),dom#(i,0,0),-20,dom#(i,1,0)
							TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
							TOKRB_SetVelocity tokobj(i),0,0,0
							TOKRB_GravityEnable tokobj(i),False
							domstatus(i)=0
						EndIf
					EndIf
				
				Next
				
			EndIf
			
			TOKSIM_Advance(2.3/FPS,2)
			UpdateWorld
		Next

	CameraZoom cam,zoom#
	PointEntity cam,centerpoint

	RenderWorld tween
	
	Text 0,0,gametimer+timeofpause-MilliSecs()
	
	Flip False
	
Wend
	
;If the player has less than 0 lives
If playerlives<0 Then
	Goto endgame
Else
	;Reset the last domino piece
	TOKRB_SetPosition tokobj(1),dom#(1,0,0),2,dom#(1,1,0)
	TOKRB_SetRotation tokobj(1),0,dom#(1,0,1),0
	TOKRB_SetVelocity tokobj(1),0,0,0
	PositionEntity obj(1),TOKRB_GetX#(tokobj(1)),TOKRB_GetY#(tokobj(1)),TOKRB_GetZ#(tokobj(1)),True
	RotateEntity obj(1),TOKRB_GetPitch#(tokobj(1)),TOKRB_GetYaw#(tokobj(1)),TOKRB_GetRoll#(tokobj(1)),True
	TOKRB_GravityEnable tokobj(1),True
	domalpha(1)=2
	EntityAlpha obj(1),domalpha(1)
	ShowEntity obj(1)
	domstatus(1)=1
	
	;Reset enemies

	Goto levelstart
EndIf


.levelcomplete
;----
;Move player to the position of the last (invisible) domino
;----


;----
;Push domino over
;----
period=1000/FPS
time=MilliSecs()-period
imptime=MilliSecs()

;First, reposition all dominoes in their correct places, in case one is out of position

;This shouldn't take more than 10 seconds, but if it does, assume something has gone wrong (a domino not fallen over) and complete the level anyway.

;Push the domino outside of the loop so it only happens once
TOKRB_ApplyImpulse2 tokobj(domcount),forcex*40,0,forcez*40,TOKRB_GetX#(tokobj(domcount)),TOKRB_GetY#(tokobj(domcount))+2,TOKRB_GetZ#(tokobj(domcount))

While Not KeyHit(1)

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
		For k=1 To ticks
			time=time+period
			If k=ticks Then
				CaptureWorld

				For i=1 To domcount
					;Position the tok dominoes
					PositionEntity obj(i),TOKRB_GetX#(tokobj(i)),TOKRB_GetY#(tokobj(i)),TOKRB_GetZ#(tokobj(i)),True
					RotateEntity obj(i),TOKRB_GetPitch#(tokobj(i)),TOKRB_GetYaw#(tokobj(i)),TOKRB_GetRoll#(tokobj(i)),True
					
					;If a domino has fallen over
					If EntityRoll(obj(i)) > 60 Or EntityRoll(obj(i)) < -60
						;as domstatus means nothing in this loop, and because it will be overwritten when the next level is loaded, we use it here to just store a variabke
						If domstatus(i)<>5
							playerscore=playerscore+levelbonus
							domstatus(i)=5
						EndIf
					EndIf

				
				Next

			EndIf
			
			TOKSIM_Advance(2.3/FPS,2)
			UpdateWorld
		Next

	CameraZoom cam,zoom#
	PointEntity cam,centerpoint

	RenderWorld tween
	
	Text 0,70,"Lives= "+playerlives
	Text 0,80,"Score= "+playerscore
	Text 0,90,"Bonus= "+levelbonus
	
	Flip False

Wend


.cleanlevel
;----
;Used to free tokamak objects from current level, and reset all other objects, ready for next level or restaring game. Called with Gosub
;----
;Dominoes
For i=1 To domcount
	TOKRB_SetPosition tokobj(i),dom#(i,0,0),-20,dom#(i,1,0)
	TOKRB_SetRotation tokobj(i),0,dom#(i,0,1),0
	TOKRB_SetVelocity tokobj(i),0,0,0
	TOKRB_GravityEnable tokobj(i),False
	domstatus(i)=0
	toklocx#=TOKRB_GetX#(tokobj(i))
	toklocz#=TOKRB_GetZ#(tokobj(i))
	PositionEntity obj(i),toklocx#,TOKRB_GetY#(tokobj(i)),toklocz#,True
	RotateEntity obj(i),TOKRB_GetPitch#(tokobj(i)),TOKRB_GetYaw#(tokobj(i)),TOKRB_GetRoll#(tokobj(i)),True
	TOKRB_Free(tokobj(i))
Next

;Player
TOKRB_SetVelocity tokplayer,0,0,0

Return

.endgame
;----
;Draw poem on screen, exit to main menu
;----

.exitgame
;----
;Exit Game
;----

TOKSIM_DestroySimulator()

End

;----
;Functions
;----

Function CreateFloor(x#,z#)
sprite=CreateMesh()
he=CreateBrush(255,255,255)
v=CreateSurface(sprite,he)
FreeBrush he
AddVertex ( v,(x#/2)*-1,0,z#/2,			0,0)
AddVertex ( v,x#/2,0,z/2,				1,0)
AddVertex ( v,x#/2,0,(z#/2)*-1,			1,1)
AddVertex ( v,(x#/2)*-1,0,(z#/2)*-1,	0,1)
AddTriangle( v,0,1,2)
AddTriangle( v,2,3,0)
Return sprite
End Function

; Display text
Function txt(x,y,texts$,colour)
 ; colour is a number from 0 to the number of different/coloured
 ; fonts in your image (the example below shows 4)

  width=13 ; Width of each font character in pixels

  For i=0 To Len(texts$)-1
   cr=(Asc(Mid$(texts$,i+1,1))-32)

     DrawImage fontimage,x,y,cr+(colour*96)
     x=x+width

  Next
End Function

; Display text for a menu (menu text always displays in the centre of the screen)
Function menutxt(x#,y,texts$,colour)
 ; colour is a number from 0 to the number of different/coloured
 ; fonts in your image (the example below shows 4)
  
  width=26 ; Width of each font character in pixels

  If x#=0 Then x#=(GraphicsWidth()/2)-((Len(texts$)*width)/2)

  For i=0 To Len(texts$)-1
   cr=(Asc(Mid$(texts$,i+1,1))-32)

     DrawImage menufontimage,x,y,cr+(colour*96)
     x=x+width

  Next
End Function