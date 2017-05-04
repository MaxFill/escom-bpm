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
				return Number(document.getElementById('productform:maxFileSize').value)/1024/1024;
			},
			/*перед сохранением получаем изображение в виде base64 и сохраняем их на специальном hidden input-е*/
			prepareScannedImageToSave: function () {
			    var imageHidden = document.getElementById('productform:image');
			    console.log('Подготовка к сканированию');
				if (Neoflex.dwThumb.selectAllImages()) {
				    console.log(Math.ceil(Neoflex.dwThumb.GetSelectedImagesSize(EnumDWT_ImageType[Neoflex.imageType])/1024/1024) + " size of selected images");
				    var imagedata = Neoflex.dwThumb.SaveSelectedImagesToBase64Binary();
				    imageHidden.value += imagedata;
				} else {
					alert("error while selecting images")
				}
			},
			/*Валидация изображений, их количества и устанавливаем доступность кнопок*/
			validateScanControls: function () {
                                console.log('Установка контролов');
				if (this.dwThumb.SourceCount == 0) {
					alert("Ошибка подключения сканера. Проверьте настройки и повторите попытку!");
					return;
				} else {
					PrimeFaces.widgets.scanButtonWidget.enable();
				}
				
				if (this.dwThumb.HowManyImagesInBuffer == 0) {
					PrimeFaces.widgets.saveButtonWidget.disable();
					PrimeFaces.widgets.deleteButtonWidget.disable();
				} else {
					PrimeFaces.widgets.deleteButtonWidget.enable();

					var imagesSizeMb = this.bufferedImagesSizeMb(),
						maxImagesSize = this.getMaxFileSize();
					if (maxImagesSize < imagesSizeMb) {
						PrimeFaces.widgets.saveButtonWidget.disable();
						alert("Scanned images are bigger than " + maxImagesSize + "Mb");
					} else { 
						PrimeFaces.widgets.saveButtonWidget.enable();
					}
				}
			},
			getProductKey: function () {
                                console.log('Получение ключа продукта');
				return document.getElementById('productform:productKey').value;
			},
			isTrial: function () {
                                console.log('Получение вида ключа продукта');
				return document.getElementById('productform:isTrial').value;
			}
		};
		
		/* Переписывает методы-загрузкчики js-скриптов и css, так как Dynamsoft считает, что они должны лежать там же, где лежит в ресурсах страница*/
		Dynamsoft.Lib.getScript_original = Dynamsoft.Lib.getScript; 
		Dynamsoft.Lib.getScript = function (g, e, i) { 
			var scripts = document.scripts;
			var j = 0;
		    for (j = 0, max = scripts.length; j < max; j++) {
		        if (scripts[j].src.indexOf(g) > -1) {
		        	Dynamsoft.WebTwainEnv.ResourcesPath = scripts[j].src.substr(0,scripts[j].src.indexOf(g)-1);
		        	i();
		        	break;
		        }
		    }
		    if (j == scripts.length) {
		    	this.getScript_original(g,e,i);
		    }
		}
		Dynamsoft.Lib.getCss_original = Dynamsoft.Lib.getCss;
		Dynamsoft.Lib.getCss = function (f) { 
			var styles = document.styleSheets;
		    for (var i = 0, max = styles.length; i < max; i++) {
		        if (styles[i].href.indexOf(f) > -1) {
		            return;
		        }
		    }
			this.getCss_original(f);
		}
		
		/*Устаналиваем необходимые нам параметры инициализации DWTwain*/
                console.log('Установка параметров инийиализации TWAIN');
		Dynamsoft.WebTwainEnv.Containers = [{ContainerId:'dwtControlThumb', Width: '100%', Height: 150}, 
		                                    {ContainerId:'dwtControlViewer', Width: '100%', Height: 350}];
		Dynamsoft.WebTwainEnv.ProductKey = Neoflex.getProductKey();
		Dynamsoft.WebTwainEnv.Trial = Neoflex.isTrial();
		Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', function () {
			Neoflex.dwThumb = Dynamsoft.WebTwainEnv.GetWebTwain('dwtControlThumb');
			if (Neoflex.dwThumb) {
		        if (Neoflex.dwThumb.ErrorCode == 0) {   
					Neoflex.dwThumb.SetViewMode(5, -1);
					Neoflex.dwThumb.RegisterEvent("OnMouseClick", function (index) {
						Neoflex.dwThumb.CopyToClipboard(index);  // Copy the image you just clicked on to the clipboard  
						Neoflex.dwViewer.LoadDibFromClipboard(); 
					});
					Neoflex.dwThumb.RegisterEvent('OnPostTransfer', function(){
					});
					Neoflex.dwThumb.RegisterEvent('OnPostAllTransfers', function(){
						Neoflex.dwThumb.selectAllImages();
						Neoflex.validateScanControls();
					});
					Neoflex.dwThumb.selectAllImages = function () {
					    Neoflex.dwThumb.SelectedImagesCount = Neoflex.dwThumb.HowManyImagesInBuffer;
					    var result = true;
					    for (var i = 0; i<Neoflex.dwThumb.HowManyImagesInBuffer; i++){
					    	result &= Neoflex.dwThumb.SetSelectedImageIndex(i, i);
					    }
					    return result;
					}
					
					Neoflex.dwViewer = Dynamsoft.WebTwainEnv.GetWebTwain('dwtControlViewer');
					Neoflex.dwViewer.SetViewMode(1, 1);
					Neoflex.dwViewer.MaxImagesInBuffer = 1;
					
					PrimeFaces.widgets.saveButtonWidget.disable();
					PrimeFaces.widgets.deleteButtonWidget.disable();
					PrimeFaces.widgets.scanButtonWidget.disable();
					Neoflex.validateScanControls();
					
					var scanButton = document.getElementById(PrimeFaces.widgets.scanButtonWidget.id);
					if (scanButton) {
						scanButton.addEventListener("click", function() {
							if (Neoflex.dwThumb) { 
								Neoflex.dwThumb.IfDisableSourceAfterAcquire = true; 
								Neoflex.dwThumb.SelectSource(); 
								Neoflex.dwThumb.OpenSource(); 
								Neoflex.dwThumb.AcquireImage(); 
							} 
						});
					}
					var deleteButton = document.getElementById(PrimeFaces.widgets.deleteButtonWidget.id);
					if (deleteButton) {
						deleteButton.addEventListener("click", function() {
							if (Neoflex.dwThumb.CurrentImageIndexInBuffer > -1) { 
								var t = Neoflex.dwThumb.RemoveImage(Neoflex.dwThumb.CurrentImageIndexInBuffer);
								var t1 = Neoflex.dwViewer.RemoveImage(0);
								Neoflex.validateScanControls();
							}
						});
					}
		        }  else {
		        	console.log("error while initialize DWTwain " + Neoflex.dwThumb.ErrorCode + ": " + Neoflex.dwThumb.ErrorString);
/*		        	var errorContrainer = $('#dwtControlViewer-initError')[0];
		        	errorContrainer.show();*/
		        }
			}
		});
		
		Dynamsoft.WebTwainEnv.Load();
	};
})();