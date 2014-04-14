Graphics3D 640,480,0,2

SetBuffer BackBuffer()
Global cam = CreateCamera()
CameraProjMode cam,2

CameraRange cam,1,100
PositionEntity cam,0,28,-47
zoom#=0.028

l=CreateLight()
centerpoint=CreatePivot()

ground = CreateFloor(90,120)
EntityColor ground,0,0,255

EntityPickMode ground,2

;Define playfield edges
playleft#=-34
playright#=34
playup#=25
playdown#=-51

playheight#=5

testerup=CreateCube()
ScaleEntity testerup,((playleft*-1)+playright)/2,playheight/2,0.5
PositionEntity testerup,0,playheight/2,playup
EntityAlpha testerup,0.3
testerdown=CreateCube()
ScaleEntity testerdown,((playleft*-1)+playright)/2,playheight/2,0.5
PositionEntity testerdown,0,playheight/2,playdown
EntityAlpha testerdown,0.3
testerleft=CreateCube()
ScaleEntity testerleft,0.5,playheight/2,((playdown*-1)+playup)/2
PositionEntity testerleft,playleft,playheight/2,playup-((playdown*-1)+playup)/2
EntityAlpha testerleft,0.3
testerright=CreateCube()
ScaleEntity testerright,0.5,playheight/2,((playdown*-1)+playup)/2
PositionEntity testerright,playright,playheight/2,playup-((playdown*-1)+playup)/2
EntityAlpha testerright,0.3


middle=CreateSphere()

mousepos=CreateCube()

;The distance between each domino
dist#=3

;Getting Information
Print "Domino Man 2006 level editor v1.0"
Print ""
domnum=Input$("How many dominoes will be in this level (not including the hidden ones) MAX=50 ? ")
RenderWorld
Flip
levelnum=Input$("What level number will this be? ")
RenderWorld
Flip

;make dummy objects representing the dominoes
Dim domobj#(domnum+1)

For i=0 To domnum+1
	domobj(i)=CreateCube()
Next

EntityColor domobj(0),255,0,0
EntityColor domobj(domnum+1),0,255,0

Dim domarray#(domnum+1,1)
;There is one additional fields in the level data, because 0 is included, but not usually counted by a player
;The first declares the number of dominos in the level (domnum), but we won't store that in this temporary array.
;The next is the direction of the pushing force, but we won't store that in the array either

;The first (0) therefore will be the 'virtual domino' used to set the rotation of the first actual domino
;The other (domnum+1) extra space is the final entry, a virtual domino to align the last domino to align properly.

domcount=0

While Not KeyHit(1)

	CameraPick(cam,MouseX(),MouseY())
	XPick#=PickedX#()
	ZPick#=PickedZ#()
	
	;after the first item has been placed, restrict the mouse from moving a certain distance from the item
	If domcount=0
		PositionEntity mousepos,XPick#,0,Zpick#
		msg$="Place what will be the master domino (the last to fall in the chain)
	ElseIf domcount<=domnum+1
		domangle#=ATan2(XPick#-EntityX#(domobj#(domcount-1)),EntityZ#(domobj#(domcount-1))-ZPick#)
		newX#=EntityX#(domobj#(domcount-1))+dist#*(Sin(domangle#))
		newZ#=EntityZ#(domobj#(domcount-1))-dist#*(Cos(domangle#))
		PositionEntity mousepos,newX#,0,newZ#
	Else
		msg$="Move design with cursor keys, and press Ctrl to save level"
		If KeyHit(29) ;Press Ctrl to save
			Goto savelevel
		EndIf
		
		If KeyDown(200) ;Up
			For i=0 To domnum+1
				;domarray#(i,0)=EntityX#(mousepos)
				domarray#(i,1)=domarray#(i,1)+0.2
				PositionEntity domobj(i),domarray(i,0),0,domarray(i,1)
			Next
		EndIf
		If KeyDown(208) ;Down
			For i=0 To domnum+1
				;domarray#(i,0)=EntityX#(mousepos)
				domarray#(i,1)=domarray#(i,1)-0.2
				PositionEntity domobj(i),domarray(i,0),0,domarray(i,1)
			Next
		EndIf
		If KeyDown(203) ;Left
			For i=0 To domnum+1
				domarray#(i,0)=domarray#(i,0)-0.2
				PositionEntity domobj(i),domarray(i,0),0,domarray(i,1)
			Next
		EndIf
		If KeyDown(205) ;Right
			For i=0 To domnum+1
				domarray#(i,0)=domarray#(i,0)+0.2
				PositionEntity domobj(i),domarray(i,0),0,domarray(i,1)
			Next
		EndIf
		
	EndIf

	If MouseHit(1) And domcount<=domnum+1

		domarray#(domcount,0)=EntityX#(mousepos)
		domarray#(domcount,1)=EntityZ#(mousepos)
		
		;For i=0 To domnum+1
		;	domobj(i)=CreateCube()
		;Next
		
		MoveEntity domobj(domcount),domarray(domcount,0),0,domarray(domcount,1)
		
		domcount=domcount+1
		
	EndIf
		
CameraZoom cam,zoom#
PointEntity cam,centerpoint
RenderWorld

Text 0,0,XPick# + ", "+ ZPick#
Text 0,20,"Maximum number dominoes: " + domnum
Text 0,30,"Domcount: " + domcount
Text 0,50,"Dominoes currently used: " + (domcount-1)

Text 0,70,msg$

Text 0,60,domangle#

Flip
Wend

End

.savelevel
Cls

FlushKeys()

	fileout = WriteFile("level"+levelnum+".dmm")
	WriteInt (fileout,domnum)
	WriteFloat (fileout,dist#)
	
	;Loop through array
	For i=0 To domnum+1
		WriteFloat (fileout,domarray#(i,0))
		WriteFloat (fileout,domarray#(i,1))
	Next

	CloseFile( fileout )
	
Print "Saved as level" + levelnum + ".dmm. Press a key to exit"

Flip

key=WaitKey()

End

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