var console = window['console']?window['console']:{'log':function(){}};
(function () {
	window.onload=function(){
		window.Neoflex = {
			dwThumb: null,
                        dwViewer: null,
			/* todo: придумать, как передавать тип файла, не привязываясь к конкретному виджету
			 * */
			imageType: PrimeFaces.widgets.fileExtensionWidget.input[0].value,
			changeFileExtension: function () {
                            console.log('Инициализация расширений');
				Neoflex.imageType = PrimeFaces.widgets.fileExtensionWidget.input[0].value;
				Neoflex.validateScanControls();
			},
			bufferedImagesSizeMb: function () {
                                console.log('Инициализация размера буфера изображения');
				return Neoflex.dwThumb.GetSelectedImagesSize(EnumDWT_ImageType[Neoflex.imageType])/1024/1024;
			},
			getMaxFileSize: function () {
                                console.log('Инициализация макс размера файла');
				return Number(document.getElementById('scanform:maxFileSize').value)/1024/1024;
			},
			/*перед сохранением получаем изображение в виде base64 и сохраняем их на специальном hidden input-е*/
			prepareScannedImageToSave: function () {
			    var imageHidden = document.getElementById('scanform:image');
			    console.log('Подготовка к сохранению образа...');
				if (Neoflex.dwThumb.selectAllImages()) {
				    console.log(Math.ceil(Neoflex.dwThumb.GetSelectedImagesSize(EnumDWT_ImageType[Neoflex.imageType])/1024/1024) + " size of selected images");
				    var imagedata = Neoflex.dwThumb.SaveSelectedImagesToBase64Binary();
				    imageHidden.value += imagedata;
				} else {
                                    alert("error while selecting images")
				}
			},
			/* Валидация изображений, их количества и устанавливаем доступность кнопок */
			validateScanControls: function () {
                                console.log('Установка контролов');
                                PrimeFaces.widgets.saveBtnWidget.disable();
                                PrimeFaces.widgets.deleteBtnWidget.disable();
                                PrimeFaces.widgets.deleteAllBtnWidget.disable();
                                PrimeFaces.widgets.rotateLeftBtnWidget.disable();
                                PrimeFaces.widgets.rotateRightBtnWidget.disable();
                                PrimeFaces.widgets.rotate180BtnWidget.disable();
                                PrimeFaces.widgets.editorBtnWidget.disable();
                                PrimeFaces.widgets.scanBtnWidget.disable();
                                
				if (this.dwThumb.SourceCount == 0) {
                                    alert("Ошибка подключения сканера. Проверьте подключение сканера и повторите попытку!");
                                    return;
				} else {
                                    PrimeFaces.widgets.scanBtnWidget.enable();
				}
				
				if (this.dwThumb.HowManyImagesInBuffer > 0) {                                
                                    PrimeFaces.widgets.editorBtnWidget.enable();
                                    PrimeFaces.widgets.deleteBtnWidget.enable();
                                    PrimeFaces.widgets.deleteAllBtnWidget.enable();
                                    PrimeFaces.widgets.rotateLeftBtnWidget.enable();
                                    PrimeFaces.widgets.rotateRightBtnWidget.enable();
                                    PrimeFaces.widgets.rotate180BtnWidget.enable();
                                    var imagesSizeMb = this.bufferedImagesSizeMb(),
                                            maxImagesSize = this.getMaxFileSize();
                                    if (maxImagesSize < imagesSizeMb) {
                                            PrimeFaces.widgets.saveBtnWidget.disable();
                                            alert("Scanned images are bigger than " + maxImagesSize + "Mb");
                                    } else { 
                                            PrimeFaces.widgets.saveBtnWidget.enable();
                                    }
				}                                
			},
			getProductKey: function () {
                                console.log('Получение ключа продукта');
				return document.getElementById('scanform:productKey').value;
			},
			isTrial: function () {
                                console.log('Получение вида ключа продукта');
				return document.getElementById('scanform:isTrial').value;
			}
		};
		
		/* Устаналиваем необходимые нам параметры инициализации DWTwain */
                console.log('Установка параметров инициализации TWAIN ...');		
		Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', initDTW);
		Dynamsoft.WebTwainEnv.Load();
                console.log('WebTwainEnv.Load ok!');		
	};
})(); 

function initDTW(){
    console.log('RegisterEvent start!');
    Neoflex.dwThumb = Dynamsoft.WebTwainEnv.GetWebTwain('dwtcontrolContainer');
    console.log('RegisterEvent work. ');
    if (Neoflex.dwThumb) {
        Neoflex.dwThumb.SetViewMode(1, 3);
        console.log('RegisterEvent work.. ');
        Neoflex.dwViewer = Dynamsoft.WebTwainEnv.GetWebTwain('dwtcontrolContainerLargeViewer'); 
        if (Neoflex.dwViewer){
            console.log('LargeViewer is ok!');
        }
        Neoflex.dwViewer.SetViewMode(-1, -1); 
        Neoflex.dwViewer.MaxImagesInBuffer = 1;
        var count = Neoflex.dwThumb.SourceCount;
        if(count == 0 && Dynamsoft.Lib.env.bMac){
            Neoflex.dwThumb.CloseSourceManager();
            Neoflex.dwThumb.ImageCaptureDriverType = 0;
            Neoflex.dwThumb.OpenSourceManager();
            count = Neoflex.dwThumb.SourceCount;
        }
        for (var i = 0; i < count; i++){
            document.getElementById("source").options.add(new Option(Neoflex.dwThumb.GetSourceNameItems(i), i));
        }
        if (Neoflex.dwThumb.ErrorCode == 0) {   
            console.log('RegisterEvent work... ');
            //Neoflex.dwThumb.SetViewMode(5, -1);

            Neoflex.dwThumb.RegisterEvent('OnPostTransfer', function(){
                Neoflex.validateScanControls(); 
                updatePageInfo();
            });
            Neoflex.dwThumb.RegisterEvent('OnPostAllTransfers', function(){
                Neoflex.dwThumb.selectAllImages();
                Neoflex.validateScanControls();
                updatePageInfo();
            });
            Neoflex.dwThumb.RegisterEvent("OnMouseClick", function(){
                updateLargeViewer();
                updatePageInfo();
            });
            Neoflex.dwThumb.selectAllImages = function () {
                Neoflex.dwThumb.SelectedImagesCount = Neoflex.dwThumb.HowManyImagesInBuffer;
                var result = true;
                for (var i = 0; i<Neoflex.dwThumb.HowManyImagesInBuffer; i++){
                    result &= Neoflex.dwThumb.SetSelectedImageIndex(i, i);
                }
                return result;
            }
            Neoflex.dwThumb.RegisterEvent("OnTopImageInTheViewChanged", function (index) {
                Neoflex.dwThumb.CurrentImageIndexInBuffer = index;
                updatePageInfo();
            });
            console.log('RegisterEvent work.... ');            

            Neoflex.validateScanControls();
            updatePageInfo();
            
            console.log('RegisterEvent work.....');
            var scanButton = document.getElementById(PrimeFaces.widgets.scanBtnWidget.id);
            if (scanButton) {
                scanButton.addEventListener("click", function() {
                    if(Neoflex.dwThumb) {
                        var OnAcquireImageSuccess, OnAcquireImageFailure;
                        OnAcquireImageSuccess = OnAcquireImageFailure = function (){
                                Neoflex.dwThumb.CloseSource();
                        };
                        Neoflex.dwThumb.IfDisableSourceAfterAcquire = true;                            
                        //If show UI
                        if (PrimeFaces.widgets.myCheckboxWidget.input.is(':checked')){
                        //if (document.getElementById("scanform:cbShowUI").checked){
                            Neoflex.dwThumb.IfShowUI = true; //Enable the Data Source's default User Interface
                        } else {
                            Neoflex.dwThumb.IfShowUI = false; //Disable the Data Source's default User Interface
                        }

                        Neoflex.dwThumb.SelectSourceByIndex(document.getElementById("source").selectedIndex);                                 
                        Neoflex.dwThumb.OpenSource();
                        Neoflex.dwThumb.AcquireImage(OnAcquireImageSuccess, OnAcquireImageFailure);
                    } 
                });
            }
            var deleteButton = document.getElementById(PrimeFaces.widgets.deleteBtnWidget.id);
            if (deleteButton) {
                deleteButton.addEventListener("click", function() {
                    if (Neoflex.dwThumb) {
                        Neoflex.dwThumb.RemoveAllSelectedImages();
                        Neoflex.dwViewer.RemoveAllImages();
                        Neoflex.validateScanControls();
                        updatePageInfo();
                    }                       
                });
            }
            var deleteButtonAll = document.getElementById(PrimeFaces.widgets.deleteAllBtnWidget.id);
            if (deleteButtonAll){
                deleteButtonAll.addEventListener("click", function() {
                    if (Neoflex.dwThumb) {
                        Neoflex.dwThumb.RemoveAllImages();
                        Neoflex.dwViewer.RemoveAllImages();
                        document.getElementById("scanform:DW_TotalImage").value = "0";
                        document.getElementById("scanform:DW_CurrentImage").value = "0";
                        Neoflex.validateScanControls();
                    }
                });
            }
            console.log('RegisterEvent work finished!');
        } else {
                console.log("error while initialize DWTwain " + Neoflex.dwThumb.ErrorCode + ": " + Neoflex.dwThumb.ErrorString);
/*		        	var errorContrainer = $('#dwtControlViewer-initError')[0];
                errorContrainer.show();*/
        }
    }
}
    
    //Callback functions for async APIs
    function OnSuccess() {
        console.log('successful');
    }

    function OnFailure(errorCode, errorString) {
        alert(errorString);
    }

    function btnPreImage_onclick() {
        if (Neoflex.dwThumb) {
            if (Neoflex.dwThumb.HowManyImagesInBuffer > 0) {
                Neoflex.dwThumb.CurrentImageIndexInBuffer = Neoflex.dwThumb.CurrentImageIndexInBuffer - 1;
                updatePageInfo();
            }
        }
    }

    function btnNextImage_onclick() {
        if (Neoflex.dwThumb) {
            if (Neoflex.dwThumb.HowManyImagesInBuffer > 0) {
                Neoflex.dwThumb.CurrentImageIndexInBuffer = Neoflex.dwThumb.CurrentImageIndexInBuffer + 1;
                updatePageInfo();
            }
        }
    }

    function btnFirstImage_onclick() {
        if (Neoflex.dwThumb) {
            if (Neoflex.dwThumb.HowManyImagesInBuffer != 0 && Neoflex.dwThumb.CurrentImageIndexInBuffer != 0) {
                Neoflex.dwThumb.CurrentImageIndexInBuffer = 0;
                updatePageInfo();
            }
        }
    }

    function btnLastImage_onclick() {
        if (Neoflex.dwThumb) {
            if (Neoflex.dwThumb.HowManyImagesInBuffer > 0) {
                Neoflex.dwThumb.CurrentImageIndexInBuffer = Neoflex.dwThumb.HowManyImagesInBuffer - 1;
                updatePageInfo();
            }
        }
    }

    function setlPreviewMode() {
        if (Neoflex.dwThumb) {
            var s = parseInt(document.getElementById("DW_PreviewMode").selectedIndex + 1);
            console.log('s=' + s);
            Neoflex.dwThumb.SetViewMode(1, s);
        }
    }

    function Rotate180() {
        RotateLeft();
        RotateLeft();
    }

    function RotateLeft() {
        if (Neoflex.dwThumb) 
            if (Neoflex.dwThumb.HowManyImagesInBuffer > 0){
                Neoflex.dwThumb.RotateLeft(Neoflex.dwThumb.CurrentImageIndexInBuffer);
            }
        updateLargeViewer();        
    }

    function RotateRight() {
        if (Neoflex.dwThumb) 
            if (Neoflex.dwThumb.HowManyImagesInBuffer > 0){
                Neoflex.dwThumb.RotateRight(Neoflex.dwThumb.CurrentImageIndexInBuffer);
            }
        updateLargeViewer();
    }

    function updatePageInfo() {
        if (Neoflex.dwThumb) {
            document.getElementById("scanform:DW_TotalImage").value = Neoflex.dwThumb.HowManyImagesInBuffer;
            document.getElementById("scanform:DW_CurrentImage").value = Neoflex.dwThumb.CurrentImageIndexInBuffer + 1;
            updateLargeViewer();
        }
    }

    function updateLargeViewer() {
        Neoflex.dwThumb.CopyToClipboard(Neoflex.dwThumb.CurrentImageIndexInBuffer); 
        Neoflex.dwViewer.LoadDibFromClipboard(); 
    }

    function ShowImageEditor() {
        if (Neoflex.dwThumb) {
            if (Neoflex.dwThumb.HowManyImagesInBuffer == 0)
                alert("There is no image in buffer.");
            else{
                Neoflex.dwViewer.RemoveAllImages();
                Neoflex.dwThumb.ShowImageEditor();
            }
        }
    }