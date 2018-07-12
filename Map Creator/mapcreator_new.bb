Global ResWidth% = 910
Global ResHeight% = 660
Global versionnumber$ = "2.1"

Loadingwindow=CreateWindow("", GraphicsWidth()/2-160,GraphicsHeight()/2-120,320,260,winhandle,8)
panelloading = CreatePanel(0,0,320,260,Loadingwindow,0)
SetPanelImage(panelloading,"map_logo.jpg")

; create a window to put the toolbar in
WinHandle=CreateWindow("SCP-CB Map Creator "+versionnumber,GraphicsWidth()/2-ResWidth/2, GraphicsHeight()/2-ResHeight/2,ResWidth,ResHeight,0, 13) 
Global MainHwnd = GetActiveWindow();User32.dll
HideGadget WinHandle

Global FileLocation$ = "..\Data\rooms.ini"
LoadRoomTemplates(FileLocation)

Global listbox = CreateListBox(5,60,ResWidth/4,ResHeight/2-20, winhandle)
; ein paar Eintrage hinzufugen
For rt.RoomTemplates = Each RoomTemplates
	;If rt\Name <> "start"
		AddGadgetItem listbox, rt\Name
	;EndIf
Next
SetGadgetLayout listbox, 3,3,2,2

InitEvents("..\Data\events.ini")
AddEvents()
;room_desc = CreateLabel("Room description:",5,40+ResHeight/2,ResWidth/4,ResHeight/8.05,WinHandle,3)
Global room_desc = CreateLabel("Описание комнаты:",5,40+ResHeight/2,ResWidth/4,ResHeight/11.8,WinHandle,3)
SetGadgetLayout room_desc , 3,3,2,2

Global grid_room_info = CreateLabel("",5,200+Resheight/2,ResWidth/4,ResHeight/11.6,WinHandle,3) ;95
SetGadgetLayout grid_room_info , 3,3,2,2
Global ChangeGridGadget% = False
Global GridGadgetText$ = ""

Global event_desc = CreateLabel("",5,117+ResHeight/2,ResWidth/4,ResHeight/12.0,WinHandle,3) ;170	ResHeight/11.8
SetGadgetLayout event_desc , 3,3,2,2

Global event_prob = CreateSlider(6,185+ResHeight/2,ResWidth/4-2,ResHeight/38.0,WinHandle,1)
SetGadgetLayout event_prob , 3,3,2,2
SetSliderRange event_prob,0,101
DisableGadget event_prob

Global event_prob_label = CreateLabel("",5,170+ResHeight/2,ResWidth/4,ResHeight/38.0,WinHandle,3)
SetGadgetLayout event_prob_label , 3,3,2,2

menu=WindowMenu(WinHandle)

Global combobox = CreateComboBox(5, 95+ResHeight/2, ResWidth/4,ResHeight-ResHeight/1.39, winhandle) ;150

;For i = 1 To 5
;	InsertGadgetItem combobox, 0, "Event #"+i
;Next
SetGadgetLayout combobox , 3,3,2,2
DisableGadget combobox

txtbox=CreateTextField(5,40,150,20,winhandle) ;create ;textfield in that window
SetGadgetText txtbox,"" ;set ;text in that ;textfield for info
ok=CreateButton("Поиск",155,40,50,20,winhandle) ;create button
clean_txt=CreateButton("X",210,40,20,20,winhandle) ;create button

;map_2d = CreateLabel("",300,25,550,550,WinHandle,3)
;SetGadgetLayout map_2d , 3,3,2,2
Global ShowGrid% = True
Const MapWidth = 18, MapHeight = 18
map_2d = CreateCanvas(300,25,551,551,WinHandle)
Dim MapIcons(5,4)
MapIcons(ROOM1, 0)=LoadImage("room1.png")
MapIcons(ROOM2, 0)=LoadImage("room2.png")
MapIcons(ROOM2C, 0)=LoadImage("room2C.png")
MapIcons(ROOM3, 0)=LoadImage("room3.png")
MapIcons(ROOM4, 0)=LoadImage("room4.png")
For i = ROOM1 To ROOM4
	MaskImage MapIcons(i,0), 255,255,255
	MidHandle(MapIcons(i,0))
	For n = 1 To 3
		MapIcons(i,n)=CopyImage(MapIcons(i,0))
		MaskImage MapIcons(i,n), 255,255,255
		RotateImage(MapIcons(i,n),90*n)
		MidHandle(MapIcons(i,n))
	Next
Next
Dim Map.RoomTemplates(MapWidth, MapHeight)
Dim MapAngle%(MapWidth, MapHeight)
Dim MapEvent$(MapWidth, MapHeight)
Dim MapEventProb#(MapWidth, MapHeight)
;For rt.RoomTemplates = Each RoomTemplates
;	If rt\Name = "start" Then
;		Map(MapWidth/2,MapHeight)=rt
;		MapEvent(MapWidth/2,MapHeight)="alarm"
;		MapAngle(MapWidth/2,MapHeight)=180
;	EndIf
;Next
Global Grid_SelectedX#=-1.0, Grid_SelectedY#=-1.0
Dim Arrows(4)
Arrows(0) = LoadImage("arrows.png")
HandleImage Arrows(0),ImageWidth(Arrows(0))/2,ImageHeight(Arrows(0))/2
For i = 1 To 3
	Arrows(i)=CopyImage(Arrows(0))
	HandleImage Arrows(i), ImageWidth(Arrows(i))/2,ImageHeight(Arrows(i))/2
	RotateImage Arrows(i), i*90
Next

Global PlusIcon
PlusIcon = LoadImage("plus.png")
MaskImage plusicon,255,255,255
MidHandle(plusicon)

SetGadgetLayout txtbox , 3,3,3,3
SetGadgetLayout ok , 3,3,3,3
SetGadgetLayout clean_txt , 3,3,3,3
tab=CreateTabber(0,5,ResWidth/4+20,ResHeight-60,winhandle)

InsertGadgetItem(tab,0,"2D/Редактор карты")
InsertGadgetItem(tab,1,"3D/Просмотр карты")
SetGadgetLayout tab , 3,3,2,2

SetStatusText(Loadingwindow, "Запуск")
; Now create a whole bunch of menus and sub-items - first of all the FILE menu
file=CreateMenu("Файл",0,menu) ; main menu
CreateMenu "Создать",0,file ; child menu 
CreateMenu "Открыть",1,file ; child menu 
CreateMenu "",1000,file ; Use an empty string to generate separator bars
CreateMenu "Сохранить",2,file ; child menu 
CreateMenu "Сохранить как...",3,file ; child menu 
CreateMenu "",1000,file ; Use an empty string to generate separator bars
CreateMenu "Выйти",10001,file ; another child menu

options=CreateMenu("Опции",0,menu)
event_default = CreateMenu("Устанавливать события для комнат по умолчанию",15,options)

CreateMenu "",1000,options
Global adjdoor_place = CreateMenu("Отображать двери в 3D просмотре",16,options)
CreateMenu "",1000,options
CreateMenu "Настроить камеру",17,options

Local option_event = GetINIInt("options.INI","general","events_default")
If (Not option_event)
	UncheckMenu event_default
Else
	CheckMenu event_default
EndIf
Local option_adjdoors = GetINIInt("options.INI","3d scene","adjdoors_place")
If (Not option_adjdoors)
	UncheckMenu adjdoor_place
Else
	CheckMenu adjdoor_place
EndIf

; Now the Edit menu
edit=CreateMenu("&Помощь",0,menu) ; Main menu with Alt Shortcut - Use & to specify the shortcut key
CreateMenu "Справка"+Chr$(8)+"F1",6,edit ; Another Child menu with Alt Shortcut
CreateMenu "О программе"+Chr$(8)+"F12",40,edit ; Child menu with Alt Shortcut

HotKeyEvent 59,0,$1001,6

;HotKeyEvent 47,2,$1001,5
HotKeyEvent 88,0,$1001,40

; Finally, once all menus are set up / updated, we call UpdateWindowMenu to tell the OS about the menu
UpdateWindowMenu WinHandle

SetStatusText(Loadingwindow, "Создание 2D сцены...")
Optionwin=CreateWindow("Настроить камеру", GraphicsWidth()/2-160,GraphicsHeight()/2-120,300,280,winhandle,1)
HideGadget optionwin
LabelColor = CreateLabel("",5,5,285,60, optionwin,1)
LabelColor2 = CreateLabel("",5,70,285,60,optionwin,1)
LabelRange = CreateLabel("",5,135,285,60, optionwin,1) ;70
color_button = CreateButton("Изменить цвет тумана", 25,20,150,30,optionwin)
color_button2 = CreateButton("Изменить цвет курсора", 25,85,150,30,optionwin)

labelfogR=CreateLabel("R "+GetINIInt("options.INI","3d scene","bg color R"),225,15,40,15, optionwin)
labelfogG=CreateLabel("G "+GetINIInt("options.INI","3d scene","bg color G"),225,30,40,15, optionwin)
labelfogB=CreateLabel("B "+GetINIInt("options.INI","3d scene","bg color B"),225,45,40,15, optionwin)

labelcursorR=CreateLabel("R "+GetINIInt("options.INI","3d scene","cursor color R"),225,75,40,15, optionwin)
labelcursorG=CreateLabel("G "+GetINIInt("options.INI","3d scene","cursor color G"),225,90,40,15, optionwin)
labelcursorB=CreateLabel("B "+GetINIInt("options.INI","3d scene","cursor color B"),225,105,40,15, optionwin)

Global redfog = GetINIInt("options.INI","3d scene","bg color R")
Global greenfog = GetINIInt("options.INI","3d scene","bg color G")
Global bluefog = GetINIInt("options.INI","3d scene","bg color B")

Global redcursor = GetINIInt("options.INI","3d scene","cursor color R")
Global greencursor = GetINIInt("options.INI","3d scene","cursor color G")
Global bluecursor = GetINIInt("options.INI","3d scene","cursor color B")

labelrange=CreateLabel("Дальность прорисовки:",15,140,80,30, optionwin)
Global camerarange = CreateTextField(25, 170, 40, 20, optionwin)
SetGadgetText camerarange, GetINIInt("options.INI","3d scene","camera range")

;labelrange=CreateLabel("Camera Range",10,140,80,20, optionwin)
;camerarange = CreateTextField(25, 145, 40, 20, optionwin)
;SetGadgetText camerarange, GetINIInt("options.INI","3d scene","camera range")

Global vsync = CreateButton("Верх. синх.", 123, 140, 150, 30, optionwin, 2)
SetButtonState vsync, GetINIInt("options.INI","3d scene","vsync")

Global showfps = CreateButton("Отображать FPS", 123, 160, 150, 30, optionwin, 2)
SetButtonState showfps, GetINIInt("options.INI","3d scene","show fps")

cancelopt_button=CreateButton("Отмена",10,210,100,30,optionwin)
saveopt_button=CreateButton("Сохранить",185,210,100,30,optionwin) ;create button
SetStatusText(Loadingwindow, "Инициализация 3D просмотра...")
ExecFile("Window3D.exe")

Repeat
	vwprt = FindWindow("Blitz Runtime Class" , "MapCreator 3d view");User32.dll
	ShowGadget Loadingwindow
Until vwprt <> 0
SetStatusText(Loadingwindow, "Создание 3D сцены...")

SetParent(vwprt,MainHwnd);User32.dll				
api_SetWindowPos( vwprt , 0 , 5 , 30 , 895 , 560 , 1);User32.dll
ShowWindow% (vwprt ,0) ;User32.dll

HideGadget Loadingwindow
ShowGadget WinHandle

SetBuffer CanvasBuffer(map_2d)

Global MouseHit1,MouseHit2,MouseDown1

Repeat
	MouseHit1 = MouseHit(1)
	MouseHit2 = MouseHit(2)
	MouseDown1 = MouseDown(1)
	MouseDown2 = MouseDown(2)
	MouseHit3 = MouseHit(3)
	If ShowGrid
		Cls
		Local width# = GadgetWidth(map_2d)
		Local height# = GadgetHeight(map_2d)
		For x = 0 To MapWidth
			For y = 0 To MapHeight
				If GetZone(y)=0
					Color 255,255,255
				ElseIf GetZone(y)=1
					Color 255,200,200
				Else
					Color 255,255,200
				EndIf
				Rect Float(width)/Float(MapWidth+1)*x,Float(height)/Float(MapHeight+1)*y,(Float(width)/Float(MapWidth+1)),(Float(height)/Float(MapHeight+1)),True
				
				Local PrevSelectedX=Grid_SelectedX, PrevSelectedY=Grid_SelectedY
				;If x>0 And x<MapWidth And y>0 And y<MapHeight
					If (MouseX()-GadgetX(map_2d))>(Float(width)/Float(MapWidth+1)*x+GadgetX(WinHandle)) And (MouseX()-GadgetX(map_2d))<((Float(width)/Float(MapWidth+1)*x)+(Float(width)/Float(MapWidth+1))+GadgetX(WinHandle))
						Local offset% = 45
						If (MouseY()-GadgetY(map_2d))>(Float(height)/Float(MapHeight+1)*y+GadgetY(WinHandle)+offset) And (MouseY()-GadgetY(map_2d))<((Float(height)/Float(MapHeight+1)*y)+(Float(height)/Float(MapHeight+1))+GadgetY(WinHandle)+offset)
							Color 200,200,200
							Rect Float(width)/Float(MapWidth+1)*x,Float(height)/Float(MapHeight+1)*y,(Float(width)/Float(MapWidth+1)),(Float(height)/Float(MapHeight+1)),True
							If Map(x,y)=Null And SelectedGadgetItem(listbox)>-1
								x2 = Float(width)/Float(MapWidth+1)
								y2 = Float(height)/Float(MapHeight+1)
								DrawImage PlusIcon,(x2*x)+(x2/2.0)+0.5,(y2*y)+(y2/2.0)+0.5
							EndIf
							If MouseHit1
								If Grid_SelectedX=x And Grid_SelectedY=y
									;Grid_SelectedX=-1
									;Grid_SelectedY=-1
									;ChangeGridGadget = True
									;GridGadgetText = ""
								Else
									item = SelectedGadgetItem( listbox )
									If Map(x,y)<>Null ;Or item>=0
										Grid_SelectedX=x
										Grid_SelectedY=y
										ChangeGridGadget = True
										GridGadgetText = ""
										SelectGadgetItem listbox,-1
										HideGadget listbox
										ShowGadget listbox
										
										ClearGadgetItems combobox
										
										Local hasEvent% = False
										Local currEventDescr$ = ""
										For rt.RoomTemplates = Each RoomTemplates
											If rt = Map(x,y)
												For i = 0 To 5
													If rt\events[i]<>""
														InsertGadgetItem combobox, i, rt\events[i]
														hasEvent = True
													EndIf
												Next
												SetGadgetText room_desc,"Описание комнаты:"+Chr(13)+rt\Description
												Exit
											EndIf
										Next 
										
										If (Not hasEvent)
											DisableGadget combobox
											SetGadgetText event_desc, ""
											SetGadgetText event_prob_label, ""
											SetSliderValue event_prob,100
											DisableGadget event_prob
										Else
											EnableGadget combobox
											If MapEvent(x,y)<>""
												For ev.event = Each event
													If ev\name = MapEvent(x,y)
														SetGadgetText event_desc, "Описание события:"+Chr(13)+ev\description
														Exit
													EndIf
												Next
											Else
												SetGadgetText event_desc, ""
											EndIf
											SetGadgetText event_prob_label, "Шанс события: 100%"
											SetSliderValue event_prob,100
											EnableGadget event_prob
										EndIf
										
										c = CountGadgetItems( combobox )
										If c > 0 Then
											For e=0 To c-1
												If GadgetItemText(combobox,e)=MapEvent(x,y)
													SelectGadgetItem combobox,e
												EndIf
											Next
										EndIf
									EndIf
									If item>=0
										If Map(x,y)=Null
											Local room_name$ = GadgetItemText$(listbox, item)
											For rt.RoomTemplates = Each RoomTemplates
												If rt\Name = room_name
													Map(x,y)=rt
													Exit
												EndIf
											Next
											If Map(x,y)\Name = "start" Or Map(x,y)\Name = "checkpoint1" Or Map(x,y)\Name = "checkpoint2"
												MapAngle(x,y)=180
											EndIf
											item2 = SelectedGadgetItem(combobox)
											If item2>=0
												Local event_name$ = GadgetItemText$(combobox, item2)
												If event_name$<>""
													MapEvent(x,y)=event_name
													MapEventProb(x,y)=Float(SliderValue(event_prob)/100.0)
												EndIf
											EndIf
										EndIf
									EndIf
								EndIf
							EndIf
							If MouseDown2
								Grid_SelectedX=-1
								Grid_SelectedY=-1
								ChangeGridGadget = True
								GridGadgetText = ""
								SetSliderValue(event_prob,100)
								;If GadgetText(event_prob_label)<>""
								;	SetGadgetText event_prob_label,"Event chance: "+SliderValue(event_prob)+"%"
								;EndIf
								SetGadgetText event_prob_label,""
								DisableGadget event_prob
								SetGadgetText event_desc,""
								DisableGadget combobox
								ClearGadgetItems combobox
								If Map(x,y)<>Null
									Map(x,y)=Null
									MapAngle(x,y)=0
									MapEvent(x,y)=""
									MapEventProb(x,y)=0.0
								EndIf
							EndIf
							If MouseHit3
								Grid_SelectedX=-1
								Grid_SelectedY=-1
								ChangeGridGadget = True
								GridGadgetText = ""
								SetSliderValue(event_prob,100)
								;If GadgetText(event_prob_label)<>""
								;	SetGadgetText event_prob_label,"Event chance: "+SliderValue(event_prob)+"%"
								;EndIf
								SetGadgetText event_prob_label,""
								DisableGadget event_prob
								SetGadgetText event_desc,""
								DisableGadget combobox
								ClearGadgetItems combobox
							EndIf
						EndIf
					EndIf
				;EndIf
				
				;If (MouseX()-GadgetX(map_2d))>(GadgetX(WinHandle)) And (MouseX()-GadgetX(map_2d))<(Float(width)+GadgetX(WinHandle))
				;	offset% = 45
				;	If (MouseY()-GadgetY(map_2d))>(GadgetY(WinHandle)+offset) And (MouseY()-GadgetY(map_2d))<(Float(height)+GadgetY(WinHandle)+offset)
				;		If MouseHit2
				;			Grid_SelectedX=-1
				;			Grid_SelectedY=-1
				;			ChangeGridGadget = True
				;			GridGadgetText = ""
				;		EndIf
				;	EndIf
				;EndIf
				
				If Grid_SelectedX=x And Grid_SelectedY=y
					Color 150,150,150
					Rect Float(width)/Float(MapWidth+1)*x,Float(height)/Float(MapHeight+1)*y,(Float(width)/Float(MapWidth+1)),(Float(height)/Float(MapHeight+1)),True
				EndIf
				
				If Map(x,y) = Null
					;If x=0 Or x=MapWidth Or y=0 Or y=MapHeight
					;	Color 170, 170, 170
					;Else
					;	Color 90,90,90
					;EndIf
					Color 90,90,90
					Rect Float(width)/Float(MapWidth+1)*x+1,Float(height)/Float(MapHeight+1)*y+1,(Float(width)/Float(MapWidth+1))-1,(Float(height)/Float(MapHeight+1))-1,False
				Else
					x2 = Float(width)/Float(MapWidth+1)
					y2 = Float(height)/Float(MapHeight+1)
					DrawImage MapIcons(Map(x,y)\Shape,Floor(MapAngle(x,y)/90.0)),(x2*x)+(x2/2.0)+0.5,(y2*y)+(y2/2.0)+0.5
					
					If Grid_SelectedX=x And Grid_SelectedY=y
						If PrevSelectedX<>Grid_SelectedX Or PrevSelectedY<>Grid_SelectedY
							ChangeGridGadget = True
							If MapEvent(x,y)<>""
								GridGadgetText = "Имя: "+Map(x,y)\Name+Chr(13)+"Угол: "+MapAngle(x,y)+"°"+Chr(13)+"Событие: "+MapEvent(x,y)+Chr(13)+"Event Chance: "+Int(MapEventProb(x,y)*100)+"%"
								SetSliderValue(event_prob,Int(MapEventProb(x,y)*100))
							Else
								GridGadgetText = "Имя: "+Map(x,y)\Name+Chr(13)+"Угол: "+MapAngle(x,y)+"°"
								;SetSliderValue(event_prob,100)
							EndIf
							If GadgetText(event_prob_label)<>""
								SetGadgetText event_prob_label,"Шанс события: "+SliderValue(event_prob)+"%"
							EndIf
						EndIf
					EndIf
				EndIf
			Next
		Next
		If MouseDown1
			If Grid_SelectedX>-1 And Grid_SelectedY>-1
				If MouseX()>(GadgetX(map_2d)+GadgetX(WinHandle)) And MouseX()<((width)+GadgetX(map_2d)+GadgetX(WinHandle))
					offset% = 45
					If MouseY()>(GadgetY(map_2d)+GadgetY(WinHandle)+offset) And MouseY()<((height)+GadgetY(map_2d)+GadgetY(WinHandle)+offset)
						If Map(Grid_SelectedX,Grid_SelectedY)\Name<>"start"
							Local prevAngle = MapAngle(Grid_SelectedX,Grid_SelectedY)
							;Left
							If (MouseX()-GadgetX(map_2d))<(Float(width)/Float(MapWidth+1)*Grid_SelectedX+GadgetX(WinHandle))
								MapAngle(Grid_SelectedX,Grid_SelectedY)=90
							EndIf
							;Right
							If (MouseX()-GadgetX(map_2d))>((Float(width)/Float(MapWidth+1)*Grid_SelectedX)+(Float(width)/Float(MapWidth+1))+GadgetX(WinHandle))
								MapAngle(Grid_SelectedX,Grid_SelectedY)=270
							EndIf
							;Up
							offset% = 45
							If (MouseY()-GadgetY(map_2d))<(Float(height)/Float(MapHeight+1)*Grid_SelectedY+GadgetY(WinHandle)+offset)
								MapAngle(Grid_SelectedX,Grid_SelectedY)=180
							EndIf
							;Down
							If (MouseY()-GadgetY(map_2d))>((Float(height)/Float(MapHeight+1)*Grid_SelectedY)+(Float(height)/Float(MapHeight+1))+GadgetY(WinHandle)+offset)
								MapAngle(Grid_SelectedX,Grid_SelectedY)=0
							EndIf
							Local width2 = Float(width)/Float(MapWidth+1)/2.0
							Local height2 = Float(height)/Float(MapHeight+1)/2.0
							DrawImage Arrows(Floor(MapAngle(Grid_SelectedX,Grid_SelectedY)/90)),Float(width)/Float(MapWidth+1)*Grid_SelectedX+width2,Float(height)/Float(MapHeight+1)*Grid_SelectedY+height2
							If prevAngle<>MapAngle(Grid_SelectedX,Grid_SelectedY)
								ChangeGridGadget = True
								If MapEvent(Grid_SelectedX,Grid_SelectedY)<>""
									GridGadgetText="Имя: "+Map(Grid_SelectedX,Grid_SelectedY)\Name+Chr(13)+"Угол: "+MapAngle(Grid_SelectedX,Grid_SelectedY)+"°"+Chr(13)+"Событие: "+MapEvent(Grid_SelectedX,Grid_SelectedY)+Chr(13)+"Шанс события: "+Int(MapEventProb(Grid_SelectedX,Grid_SelectedY)*100)+"%"
								Else
									GridGadgetText="Имя: "+Map(Grid_SelectedX,Grid_SelectedY)\Name+Chr(13)+"Угол: "+MapAngle(Grid_SelectedX,Grid_SelectedY)+"°"
								EndIf
							EndIf
						EndIf
					EndIf
				EndIf
			EndIf
		EndIf
		FlipCanvas map_2d
	EndIf
	If Grid_SelectedX<>-1 And Grid_SelectedY<>-1
		Local prevEvent = MapEvent(Grid_SelectedX,Grid_SelectedY)
		item2 = SelectedGadgetItem(combobox)
		If item2>=0
			event_name$ = GadgetItemText$(combobox, item2)
			If event_name<>prevEvent ;And prevEvent<>""
				If event_name$<>""
					MapEvent(Grid_SelectedX,Grid_SelectedY)=event_name
					GridGadgetText="Имя: "+Map(Grid_SelectedX,Grid_SelectedY)\Name+Chr(13)+"Угол: "+MapAngle(Grid_SelectedX,Grid_SelectedY)+"°"+Chr(13)+"Событие: "+MapEvent(Grid_SelectedX,Grid_SelectedY)+Chr(13)+"Шанс события: "+Int(MapEventProb(Grid_SelectedX,Grid_SelectedY)*100)+"%"
					ChangeGridGadget=True
				EndIf
			EndIf
		EndIf
	EndIf
	If ChangeGridGadget
		SetGadgetText grid_room_info, GridGadgetText
		ChangeGridGadget=False
	EndIf
	id=WaitEvent()
	If ID=$803 And EventSource()= winhandle Then Exit ; Handle the close gadget on the window being hit
	If ID=$803 And  EventSource()= optionwin Then HideGadget optionwin
	If ID=$1001 Then ; Handle any menu item hit events
	; extract the EventData as this will contain our unique id for the menu item
	EID=EventData() 
	    If EID=0 Then 
			
			result = Proceed("Сохранить текущую карту?",True) 
			If result=1 Then
				SetStatusText(winhandle, "Создана новая карта и сохранены предыдущая")
				If FileType(filename$) <>1
  			   		filename$ = RequestFile("Открыть карту","cbmap",True,"")
				EndIf
				If filename<>""
					SaveMap(filename$)
				EndIf
				EraseMap()
				If ShowGrid=False
					SaveMap("CONFIG_MAPINIT.SI",True)
				EndIf
				filename$ = ""
			ElseIf result=0 Then 
				SetStatusText(winhandle, "Создана новая карта без сохранения предыдущей")
				EraseMap()
				If ShowGrid=False
					SaveMap("CONFIG_MAPINIT.SI",True)
				EndIf
				filename$ = ""
			ElseIf result=-1 Then
				SetStatusText(winhandle, "Создание новой карты отменено")
			EndIf
		EndIf
		If EID=1 Then
			filename$ = RequestFile("Открыть карту","cbmap",False,"") 
			If filename<>""
				LoadMap(filename$)
			Else
				;Maybe a message or something here, dunno...
			EndIf
		EndIf
		If EID=2 Then
			If FileType(filename) <>1
  			   filename$ = RequestFile("Сохранить карту","cbmap",True,"")
			EndIf
			If filename<>""
				SaveMap(filename$)
			Else
				;Maybe a message or something here, dunno...
			EndIf
		EndIf	
		If EID=3 Then
			filename$ = RequestFile("Сохранить карту","cbmap",True,"")
			If filename<>""
				SaveMap(filename$)
			Else
				;Maybe a message or something here, dunno...
			EndIf
		EndIf
		If EID=6 Then ExecFile "Manual.pdf"
		If EID=40  Then Notify "SCP Containement Breach Map Creator v"+versionnumber+""+Chr$(13)+"Разработали: Vane Brain и ENDSHN."+Chr$(13)+"Перевёл: Oleg720"
		If EID=17 Then 
			ShowGadget optionwin
		EndIf
		If EID=15
			value=MenuChecked(event_default)
			If value=0 Then CheckMenu(event_default)
			If value=1 Then UncheckMenu(event_default)
			UpdateWindowMenu winhandle
			PutINIValue("options.INI","general","events_default",Not value)
		EndIf
		If EID=16
			value=MenuChecked(adjdoor_place)
			If value=0 Then CheckMenu(adjdoor_place)
			If value=1 Then UncheckMenu(adjdoor_place)
			UpdateWindowMenu winhandle
			PutINIValue("options.INI","3d scene","adjdoors_place",Not value)
			WriteOptions()
		EndIf
		If EID=10001 Then End
	EndIf
	
	DebugLog EventData()
	If ID=$401 Then ; Button action event.  EventData contains the toolbar button hit.
		If EventSource()=tab Then
            ;in EventData steht das neue Item
            ;also in Abhangigkeit des Gadgets zeigen und verst
            Select EventData()
               Case 0
                 	ShowWindow% (vwprt ,0)
                  	ShowGadget listbox 
					ShowGadget event_desc 
					ShowGadget txtbox 
					ShowGadget ok 
					ShowGadget clean_txt
					ShowGadget combobox
					ShowGadget map_2d
					ShowGadget room_desc
					ShowGadget event_prob
					ShowGadget event_prob_label
					ShowGadget grid_room_info
					SetGadgetShape(tab, 0,5,ResWidth/4+20,ResHeight-60)
					ShowGrid = True
               Case 1
					ShowWindow% (vwprt ,1) ;User32.dll
              		HideGadget listbox 
					HideGadget event_desc 
					HideGadget txtbox 
					HideGadget ok 
					HideGadget clean_txt
					HideGadget combobox
					HideGadget map_2d
					HideGadget room_desc
					HideGadget event_prob
					HideGadget event_prob_label
					HideGadget grid_room_info
					SetGadgetShape(tab, 0,5,ResWidth,ResHeight-60)
					ShowGrid = False
					SaveMap("CONFIG_MAPINIT.SI",True)
            End Select
         EndIf
		If EventSource()=color_button Then 
			If RequestColor(GetINIInt("options.INI","3d scene","bg color R"),GetINIInt("options.INI","3d scene","bg color G"),GetINIInt("options.INI","3d scene","bg color B"))=1 Then
				redfog=RequestedRed()
				greenfog=RequestedGreen()
				bluefog=RequestedBlue()
				SetGadgetText labelfogR, "R "+redfog
				SetGadgetText labelfogG, "G "+greenfog
				SetGadgetText labelfogB, "B "+bluefog
			EndIf	
		EndIf
		If EventSource()=color_button2 Then
			If RequestColor(GetINIInt("options.INI","3d scene","cursor color R"),GetINIInt("options.INI","3d scene","cursor color G"),GetINIInt("options.INI","3d scene","cursor color B"))=1 Then
				redcursor=RequestedRed()
				greencursor=RequestedGreen()
				bluecursor=RequestedBlue()
				SetGadgetText labelcursorR, "R "+redcursor
				SetGadgetText labelcursorG, "G "+greencursor
				SetGadgetText labelcursorB, "B "+bluecursor
			EndIf
		EndIf
		If EventSource()=cancelopt_button Then
			SetGadgetText labelfogR,"R "+GetINIInt("options.INI","3d scene","bg color R")
			SetGadgetText labelfogG,"G "+GetINIInt("options.INI","3d scene","bg color G")
			SetGadgetText labelfogB,"B "+GetINIInt("options.INI","3d scene","bg color B")
			SetGadgetText labelcursorR,"R "+GetINIInt("options.INI","3d scene","cursor color R")
			SetGadgetText labelcursorG,"G "+GetINIInt("options.INI","3d scene","cursor color G")
			SetGadgetText labelcursorB,"B "+GetINIInt("options.INI","3d scene","cursor color B")
			SetGadgetText camerarange, GetINIInt("options.INI","3d scene","camera range")
			SetButtonState vsync, GetINIInt("options.INI","3d scene","vsync")
			SetButtonState showfps, GetINIInt("options.INI","3d scene","show fps")
			HideGadget optionwin
		EndIf	
		If EventSource()=saveopt_button Then
			HideGadget optionwin
			SetStatusText(winhandle, "Новые настройки сохранены")
			PutINIValue("options.INI","3d scene","bg color R",redfog)
			PutINIValue("options.INI","3d scene","bg color G",greenfog)
			PutINIValue("options.INI","3d scene","bg color B",bluefog)
			PutINIValue("options.INI","3d scene","cursor color R",redcursor)
			PutINIValue("options.INI","3d scene","cursor color G",greencursor)
			PutINIValue("options.INI","3d scene","cursor color B",bluecursor)
			PutINIValue("options.INI","3d scene","camera range",TextFieldText$(camerarange))
			PutINIValue("options.INI","3d scene","vsync",ButtonState(vsync))
			PutINIValue("options.INI","3d scene","show fps",ButtonState(showfps))
			WriteOptions()
		EndIf
		If EventSource()=ok Then ; when ok is pressed
			;Notify ""+Chr$(13)+TextFieldText$(txtbox); <---TO GET ;text FROM ;textFIELD
			ClearGadgetItems listbox
			For rt.RoomTemplates = Each RoomTemplates
				If Instr(rt\Name,TextFieldText(txtbox))
					AddGadgetItem listbox, rt\Name
				EndIf
			Next
		EndIf
		If EventSource()=clean_txt Then
			SetGadgetText txtbox, ""
			ClearGadgetItems listbox
			For rt.RoomTemplates = Each RoomTemplates
				AddGadgetItem listbox, rt\Name
			Next
		EndIf
		If EventSource() = combobox Then
			item = SelectedGadgetItem( combobox )
			
			If item > -1 Then
				
				name$ = GadgetItemText$(combobox,item)
				
				For ev.event = Each event
					If ev\name = name
						SetGadgetText event_desc, "Описание события:"+Chr(13)+ev\description
						Exit
					EndIf
				Next
			EndIf
		EndIf
		If EventSource() = listbox Then 
		    ;In Abhangigkeit des Tabs den selektierten Eintrag herausfinden
            item = SelectedGadgetItem( listbox )
            
			Grid_SelectedX=-1
			Grid_SelectedY=-1
			ChangeGridGadget = True
			GridGadgetText = ""
            
            ;Wenn ein Eintrag ausgewahlt wurde
            If item > - 1 Then
				
               ;Bezeichnung des Eintrags herausfinden
               name$ = GadgetItemText$(listbox, item)
               ;Notify name$ + " selected!"
				
				ClearGadgetItems combobox
				
				hasEvent% = False
				Local currRT.RoomTemplates = Null
				For rt.RoomTemplates = Each RoomTemplates
					If rt\Name = name
						For i = 0 To 5
							If rt\events[i]<>""
								InsertGadgetItem combobox, i, rt\events[i]
								hasEvent = True
							EndIf
						Next
						SetGadgetText room_desc,"Описание комнаты:"+Chr(13)+rt\Description
						currRT = rt
						Exit
					EndIf
				Next 
				
				If (Not hasEvent)
					DisableGadget combobox
					SetGadgetText event_desc, ""
					SetGadgetText event_prob_label, ""
					SetSliderValue event_prob,100
					DisableGadget event_prob
				Else
					EnableGadget combobox
					For ev.event = Each event
						If ev\name = currRT\events[0]
							SetGadgetText event_desc, "Описание события:"+Chr(13)+ev\description
							Exit
						EndIf
					Next
					SetGadgetText event_prob_label, "Шанс события: 100%"
					SetSliderValue event_prob,100
					EnableGadget event_prob
				EndIf
				Grid_SelectedX=-1
				Grid_SelectedY=-1
				ChangeGridGadget = True
				GridGadgetText = ""
				If MenuChecked(event_default) Then
					If CountGadgetItems( combobox ) > 0 Then
						SelectGadgetItem combobox, 0
					EndIf
				EndIf

			EndIf
		EndIf
		If EventSource()=event_prob
			SetGadgetText event_prob_label,"Шанс события: "+SliderValue(event_prob)+"%"
			If Grid_SelectedX<>-1 And Grid_SelectedY<>-1
				x=Grid_SelectedX
				y=Grid_SelectedY
				MapEventProb(x,y)=Float(SliderValue(event_prob)/100.0)
				GridGadgetText = "Имя: "+Map(x,y)\Name+Chr(13)+"Угол: "+MapAngle(x,y)+"°"+Chr(13)+"Событие: "+MapEvent(x,y)+Chr(13)+"Шанс события: "+Int(MapEventProb(x,y)*100)+"%"
				SetGadgetText grid_room_info, GridGadgetText
			EndIf
		EndIf
	EndIf
	
Forever
End




Function StripPath$(file$) 
	Local name$=""
	If Len(file$)>0 
		For i=Len(file$) To 1 Step -1 
			
			mi$=Mid$(file$,i,1) 
			If mi$="\" Or mi$="/" Then Return name$
			
			name$=mi$+name$ 
		Next 
		
	EndIf 
	
	Return name$ 
End Function

Function Piece$(s$,entry,char$=" ")
	While Instr(s,char+char)
		s=Replace(s,char+char,char)
	Wend
	For n=1 To entry-1
		p=Instr(s,char)
		s=Right(s,Len(s)-p)
	Next
	p=Instr(s,char)
	If p<1
		a$=s
	Else
		a=Left(s,p-1)
	EndIf
	Return a
End Function


Function GetINIString$(file$, section$, parameter$)
	Local TemporaryString$ = ""
	Local f = ReadFile(file)
	
	While Not Eof(f)
		If ReadLine(f) = "["+section+"]" Then
			Repeat 
				TemporaryString = ReadLine(f)
				If Trim( Left(TemporaryString, Max(Instr(TemporaryString,"=")-1,0)) ) = parameter Then
					CloseFile f
					Return Trim( Right(TemporaryString,Len(TemporaryString)-Instr(TemporaryString,"=")) )
				EndIf
			Until Left(TemporaryString,1) = "[" Or Eof(f)
			CloseFile f
			Return ""
		EndIf
	Wend
	
	CloseFile f
End Function

Function GetINIInt%(file$, section$, parameter$)
	Local strtemp$ = Lower(GetINIString(file$, section$, parameter$))
	
	Select strtemp
		Case "true"
			Return 1
		Case "false"
			Return 0
		Default
			Return Int(strtemp)
	End Select
	Return 
End Function

Function GetINIFloat#(file$, section$, parameter$)
	Return GetINIString(file$, section$, parameter$)
End Function

Function PutINIValue%(INI_sAppName$, INI_sSection$, INI_sKey$, INI_sValue$)
	
; Returns: True (Success) or False (Failed)
	
	INI_sSection = "[" + Trim$(INI_sSection) + "]"
	INI_sUpperSection$ = Upper$(INI_sSection)
	INI_sKey = Trim$(INI_sKey)
	INI_sValue = Trim$(INI_sValue)
	INI_sFilename$ = CurrentDir$() + "\"  + INI_sAppName
	
; Retrieve the INI data (if it exists)
	
	INI_sContents$= INI_FileToString(INI_sFilename)
	
; (Re)Create the INI file updating/adding the SECTION, KEY and VALUE
	
	INI_bWrittenKey% = False
	INI_bSectionFound% = False
	INI_sCurrentSection$ = ""
	
	INI_lFileHandle = WriteFile(INI_sFilename)
	If INI_lFileHandle = 0 Then Return False ; Create file failed!
	
	INI_lOldPos% = 1
	INI_lPos% = Instr(INI_sContents, Chr$(0))
	
	While (INI_lPos <> 0)
		
		INI_sTemp$ =Trim$(Mid$(INI_sContents, INI_lOldPos, (INI_lPos - INI_lOldPos)))
		
		If (INI_sTemp <> "") Then
			
			If Left$(INI_sTemp, 1) = "[" And Right$(INI_sTemp, 1) = "]" Then
				
				; Process SECTION
				
				If (INI_sCurrentSection = INI_sUpperSection) And (INI_bWrittenKey = False) Then
					INI_bWrittenKey = INI_CreateKey(INI_lFileHandle, INI_sKey, INI_sValue)
				End If
				INI_sCurrentSection = Upper$(INI_CreateSection(INI_lFileHandle, INI_sTemp))
				If (INI_sCurrentSection = INI_sUpperSection) Then INI_bSectionFound = True
				
			Else
				
				; KEY=VALUE
				
				lEqualsPos% = Instr(INI_sTemp, "=")
				If (lEqualsPos <> 0) Then
					If (INI_sCurrentSection = INI_sUpperSection) And (Upper$(Trim$(Left$(INI_sTemp, (lEqualsPos - 1)))) = Upper$(INI_sKey)) Then
						If (INI_sValue <> "") Then INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
						INI_bWrittenKey = True
					Else
						WriteLine INI_lFileHandle, INI_sTemp
					End If
				End If
				
			End If
			
		End If
		
		; Move through the INI file...
		
		INI_lOldPos = INI_lPos + 1
		INI_lPos% = Instr(INI_sContents, Chr$(0), INI_lOldPos)
		
	Wend
	
	; KEY wasn't found in the INI file - Append a new SECTION if required and create our KEY=VALUE line
	
	If (INI_bWrittenKey = False) Then
		If (INI_bSectionFound = False) Then INI_CreateSection INI_lFileHandle, INI_sSection
		INI_CreateKey INI_lFileHandle, INI_sKey, INI_sValue
	End If
	
	CloseFile INI_lFileHandle
	
	Return True ; Success
	
End Function

Function INI_FileToString$(INI_sFilename$)
	
	INI_sString$ = ""
	INI_lFileHandle% = ReadFile(INI_sFilename)
	If INI_lFileHandle <> 0 Then
		While Not(Eof(INI_lFileHandle))
			INI_sString = INI_sString + ReadLine$(INI_lFileHandle) + Chr$(0)
		Wend
		CloseFile INI_lFileHandle
	End If
	Return INI_sString
	
End Function

Function INI_CreateSection$(INI_lFileHandle%, INI_sNewSection$)
	
	If FilePos(INI_lFileHandle) <> 0 Then WriteLine INI_lFileHandle, "" ; Blank line between sections
	WriteLine INI_lFileHandle, INI_sNewSection
	Return INI_sNewSection
	
End Function

Function INI_CreateKey%(INI_lFileHandle%, INI_sKey$, INI_sValue$)
	
	WriteLine INI_lFileHandle, INI_sKey + "=" + INI_sValue
	Return True
	
End Function

Function Min#(a#,b#)
	If a < b Then Return a Else Return b
End Function

Function Max#(a#,b#)
	If a > b Then Return a Else Return b
End Function

Const ROOM1% = 1, ROOM2% = 2, ROOM2C% = 3, ROOM3% = 4, ROOM4% = 5

Const ZONEAMOUNT = 3

Global RoomTempID%
Type RoomTemplates
	Field Shape%, Name$
	Field Description$
	Field Large%
	Field id
	
	Field events$[5]
End Type 

Function CreateRoomTemplate.RoomTemplates()
	Local rt.RoomTemplates = New RoomTemplates
	
	rt\id = RoomTempID
	RoomTempID=RoomTempID+1
	
	Return rt
End Function

Function LoadRoomTemplates(file$)
	Local TemporaryString$
	Local rt.RoomTemplates = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "[" Then
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			
			Local AddRoom% = True
			Select TemporaryString
				Case "room ambience","173","pocketdimension","dimension1499","gatea"
					AddRoom = False
			End Select
			If AddRoom
				rt = CreateRoomTemplate()
				rt\Name = TemporaryString
				
				StrTemp = Lower(GetINIString(file, TemporaryString, "shape"))
				Select StrTemp
					Case "room1", "1"
						rt\Shape = ROOM1
					Case "room2", "2"
						rt\Shape = ROOM2
					Case "room2c", "2c"
						rt\Shape = ROOM2C
					Case "room3", "3"
						rt\Shape = ROOM3
					Case "room4", "4"
						rt\Shape = ROOM4
					Default
				End Select
				
				rt\Description = GetINIString(file, TemporaryString, "descr")
				rt\Large = GetINIInt(file, TemporaryString, "large")
				
			EndIf
			
		EndIf
	Wend
	
	CloseFile f
	
End Function

Const MaxEvents = 9

Type Event
	Field Name$
	Field Description$
	Field Room$[MaxEvents]
End Type

Function InitEvents(file$)
	Local TemporaryString$
	Local e.Event = Null
	Local StrTemp$ = ""
	
	Local f = OpenFile(file)
	
	While Not Eof(f)
		TemporaryString = Trim(ReadLine(f))
		If Left(TemporaryString,1) = "["
			TemporaryString = Mid(TemporaryString, 2, Len(TemporaryString) - 2)
			
			e = New Event
			e\Name = TemporaryString
			
			e\Description = GetINIString(file, TemporaryString, "descr")
			
			For i = 1 To MaxEvents
				e\Room[i] = GetINIString(file, TemporaryString, "room"+i)
			Next
			
		EndIf
	Wend
	
	CloseFile f
	
End Function

Function AddEvents()
	Local rt.RoomTemplates,e.Event
	
	For rt.RoomTemplates = Each RoomTemplates
		For e = Each Event
			For i = 1 To MaxEvents
				If rt\Name = e\Room[i]
					AssignEventToRoomTemplate(rt,e)
				EndIf
			Next
		Next
	Next
	
End Function

Function AssignEventToRoomTemplate(rt.RoomTemplates,e.Event)
	
	For i = 0 To 5
		If rt\events[i]=""
			rt\events[i]=e\Name
			Exit
		EndIf
	Next
	
End Function

Function GetZone(y%)
	Return Min(Floor((Float(MapWidth-y)/MapWidth*ZONEAMOUNT)),ZONEAMOUNT-1)
End Function

Function EraseMap()
	Grid_SelectedX=-1
	Grid_SelectedY=-1
	ChangeGridGadget = True
	GridGadgetText = ""
	
	Local hasEvent% = False
	item = SelectedGadgetItem( listbox )
    If item > - 1 Then
    	name$ = GadgetItemText$(listbox, item)
		For rt.RoomTemplates = Each RoomTemplates
			If rt\Name = name
				For i = 0 To 5
					If rt\events[i]<>""
						hasEvent = True
					EndIf
				Next
				Exit
			EndIf
		Next 
	EndIf
	
	If (Not hasEvent)
		DisableGadget combobox
		SetGadgetText event_desc, ""
		SetGadgetText event_prob_label, ""
		SetSliderValue event_prob,100
		DisableGadget event_prob
	Else
		SetSliderValue event_prob,100
		SetGadgetText event_prob_label,"Шанс события: "+SliderValue(event_prob)+"%"
	EndIf
	
	For x = 0 To MapWidth
		For y = 0 To MapHeight
			Map(x,y)=Null
			MapAngle(x,y)=0
			MapEvent(x,y)=""
			MapEventProb(x,y)=0.0
		Next
	Next
	
End Function

Function LoadMap(file$)
	EraseMap()
	
	f% = ReadFile(file)
	DebugLog file
	
	While Not Eof(f)
		x = ReadByte(f)
		y = ReadByte(f)
		name$ = ReadString(f)
		DebugLog x+", "+y+": "+name
		For rt.roomtemplates=Each RoomTemplates
			If Lower(rt\name) = name Then
				DebugLog rt\name
				Map(x,y)=rt
				Exit
			EndIf
		Next
		MapAngle(x,y)=ReadByte(f)*90
		MapEvent(x,y) = ReadString(f)
		MapEventProb(x,y) = ReadFloat(f)
	Wend
	
	If ShowGrid=False
		SaveMap("CONFIG_MAPINIT.SI",True)
	EndIf
	
	CloseFile f
End Function

Function SaveMap(file$,streamtoprgm%=False)
	f% = WriteFile(file)
	
	For x = 0 To MapWidth
		For y = 0 To MapHeight
			If Map(x,y)<>Null
				WriteByte f, x
				WriteByte f, y
				WriteString f, Lower(Map(x,y)\Name)
				WriteByte f, Floor(MapAngle(x,y)/90.0)
				WriteString f, MapEvent(x,y)
				WriteFloat f, MapEventProb(x,y)
				
				If streamtoprgm
					If Grid_SelectedX=x And Grid_SelectedY=y
						WriteByte f,1
					Else
						WriteByte f,0
					EndIf
				EndIf
			EndIf
		Next
	Next
	
	CloseFile f
End Function

Function MilliSecs2()
	Local retVal% = MilliSecs()
	If retVal < 0 Then retVal = retVal + 2147483648
	Return retVal
End Function

Function WriteOptions()
	
	f = WriteFile("CONFIG_OPTINIT.SI")
	WriteInt f,redfog
	WriteInt f,greenfog
	WriteInt f,bluefog
	WriteInt f,redcursor
	WriteInt f,greencursor
	WriteInt f,bluecursor
	WriteInt f,TextFieldText$(camerarange)
	WriteByte f,ButtonState(vsync)
	WriteByte f,ButtonState(showfps)
	WriteByte f,MenuChecked(adjdoor_place)
	CloseFile f
	
End Function

