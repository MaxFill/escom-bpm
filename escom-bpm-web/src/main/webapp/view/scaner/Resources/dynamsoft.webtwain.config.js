//
// Dynamsoft JavaScript Library for Basic Initiation of Dynamic Web TWAIN
// More info on DWT: http://www.dynamsoft.com/Products/WebTWAIN_Overview.aspx
//
// Copyright 2017, Dynamsoft Corporation 
// Author: Dynamsoft Team
// Version: 13.1
//
/// <reference path="dynamsoft.webtwain.initiate.js" />
var Dynamsoft = Dynamsoft || { WebTwainEnv: {} };

Dynamsoft.WebTwainEnv.AutoLoad = true;

///
Dynamsoft.WebTwainEnv.Containers = [{ContainerId:'dwtcontrolContainer', Width: 239, Height:333}, 
{ContainerId:'dwtcontrolContainerLargeViewer', Width:420, Height:518}];

/// If you need to use multiple keys on the same server, you can combine keys and write like this 
/// Dynamsoft.WebTwainEnv.ProductKey = 'key1;key2;key3';
Dynamsoft.WebTwainEnv.ProductKey = '1CFD41A1823B70DD0D9632E4D3393C1A7BAD56CC0ACF6014B4196D48F86523DF0E158F3A204B741DD179A22F8A22F9270E158F3A204B741DCBEBDBEEF26C4B407BAD56CC0ACF6014C71DF6298FFDC6C440000000;t0068WQAAAAZoWeQUOUYWjRCB0dWsDEVcvm/GCZqTp8ezVC6PJdI9RrghcGJG0kDZlWmci5e4O2qmFDC4Gj2OaeXqmnYtWKE=';

///
Dynamsoft.WebTwainEnv.Trial = true;

///
Dynamsoft.WebTwainEnv.ActiveXInstallWithCAB = false;

///
// Dynamsoft.WebTwainEnv.ResourcesPath = 'Resources';

/// All callbacks are defined in the dynamsoft.webtwain.install.js file, you can customize them.
// Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', function(){
// 		// webtwain has been inited
// });

