Graphics3D 800,600,0,2

SetBuffer BackBuffer()


Global cam = CreateCamera()
PositionEntity cam,0,15,-45
zoom#=1.3

l=CreateLight()
centerpoint=CreatePivot()

ground = CreateCube()
EntityColor ground,255,0,255
ScaleEntity ground,50,1,50
PositionEntity ground,0,-1,0
EntityPickMode ground,2

middle=CreateSphere()

mousepos=CreateCube()

;Getting Information
Print "Domino Man 2006 level editor v1.0"
Print ""
levnum=Input$("What level am I loading? ")
RenderWorld
Flip

;Load the variables from the data file
filein = ReadFile("level"+levnum+".dmm")
domnum=ReadInt(filein)
forcex=ReadInt(filein)
forcez=ReadInt(filein)

Dim dom(domnum+1,1)
Dim domobj(domnum+1)

For i=0 To domnum+1
	dom(i,0) = ReadFloat (filein)
	dom(i,1) = ReadFloat (filein)
Next
CloseFile( filein )

;make dummy objects representing the dominoes
For i=0 To domnum+1
	domobj(i)=CreateCube()
	MoveEntity domobj(i),dom(i,0),0,dom(i,1)
Next

EntityColor domobj(0),255,0,0
EntityColor domobj(domnum+1),0,255,0


	
While Not KeyHit(1)
	
PointEntity cam,centerpoint
RenderWorld

Text 0,0,forcex + ", "+ forcez

Flip
Wend


End