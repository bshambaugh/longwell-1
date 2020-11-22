
var blockStati = new Array();
var expire = new Date();
expire.setTime(expire.getTime() + 365 * 24 * 60 * 60 * 1000);

function time() {
	var date = new Date();
	var time = date.getSeconds() * 1000 + date.getMilliseconds();
	return time;
}
				
function init() {
    var starttime = time();
    parseStati(getCookie("block_stati"));
    applyStati();
    var endtime = time();
    window.status = "Blocks update time: " + (endtime - starttime) + " ms";
}

function parseStati(str) {
    var tokens = str.split('|');
    for (i = 0; i < tokens.length; i++) {
        var token = tokens[i];
        var pair = token.split('=');
        blockStati[pair[0]] = pair[1];    }
}

function applyStati(stati) {
    blocks = document.getElementsByName("block");
    for (i = 0; i < blocks.length; i++) {
        var element = blocks.item(i);
        var id = element.id.split(":")[1];
        if (blockStati[id]) {
            element.style.display = blockStati[id];
            makeConsistent(id,element);
        } else {
            if (element.style.display) {
                blockStati[id] = element.style.display;
            } else {
            	blockStati[id] = "block";
            }
        }
    }
}

function serializeStati() {
    var str = "";
    for (block in blockStati) {
    	if (block) {
	        var status = blockStati[block];
	        var token = block + '=' + status;
	        str += token + '|';
	    }
    }
    return str;
}

function makeConsistent(id,element) {
	var trigger = document.getElementById("trigger:" + id);
	if (trigger) {
	    if ((element.style.display != "none") && (trigger.src.indexOf('closed') > -1)) {
	        trigger.src = trigger.src.replace(/closed/gi,"open");;
	    } else if ((element.style.display == "none") && (trigger.src.indexOf('open') > -1)) {
	        trigger.src = trigger.src.replace(/open/gi,"closed");;
	    }
	}
}

function toggle(id) {

    var block = document.getElementById("block:" + id);
    with (block.style) {
	    if (display=="none") {
	        display="block";
	    } else {
	        display="none";
	    }
	}

    makeConsistent(id,block);
    
    blockStati[id] = block.style.display;
    setCookie("block_stati", serializeStati(), expire);
}

var searchStr = "";

function getTarget(e) {
	if (!e) var e = window.event;
	if (e.target) {
	   targ = e.target;
	} else if (e.srcElement) {
	   targ = e.srcElement;
	}
	return targ;
}

function restrict(e,name) {
    var starttime = time();
    var targ = document.getElementById(e);
    var items = document.getElementsByName(name);
    var regexp = new RegExp(targ.value, "ig");
    for (i = 0; i < items.length; i++) {
        var item = items.item(i);
        if (targ.value.length > 0) {
            if (item.firstChild.nodeValue.match(regexp)) {
                if (item.style.display != "inline") {
                    item.style.display = "inline";
                }
            } else {
                if (item.style.display != "none") {
                    item.style.display = "none";
                }
            }
        } else {
            item.style.display = "inline";
        }
    }
    var endtime = time();
    window.status = "Facet update time: " + (endtime - starttime) + " ms";
}

function resetSearch(target, pass) {
    var targ = document.getElementById(target);
    targ.value = "";
    restrict(target, pass);
}

function setOrder(order) {
	document.getElementById('order').value = order; 
	document.getElementById('order.form').submit();
}

// ---------------------- cookie functions --------------------------



function setCookie(name, value, expires, path, domain, secure) {
  var curCookie = name + "=" + escape(value) +
      ((expires) ? "; expires=" + expires.toGMTString() : "") +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      ((secure) ? "; secure" : "");
  document.cookie = curCookie;
}

function getCookie(name) {
  var dc = document.cookie;
  var prefix = name + "=";
  var begin = dc.indexOf("; " + prefix);
  if (begin == -1) {
    begin = dc.indexOf(prefix);
    if (begin != 0) return "";
  } else
    begin += 2;
  var end = document.cookie.indexOf(";", begin);
  if (end == -1)
    end = dc.length;
  return unescape(dc.substring(begin + prefix.length, end));
}
