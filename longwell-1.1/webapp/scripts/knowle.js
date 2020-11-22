var expire = new Date();
expire.setTime(expire.getTime() + 365 * 24 * 60 * 60 * 1000);

function init() {
    var pref = getCookie("uriPref");
    if(document.forms["prefs"].uriPref) {
        document.forms["prefs"].uriPref.checked = (pref == 1) ? true : false;
        (pref == 1) ? setURILabel() : setShortLabel();
    }
}

function toggleLabel(el) {
    el ? setURILabel() : setShortLabel();
}

function switchLabel(id) {
    var resource = document.getElementById(id);
    var altTxt = resource.getAttribute('title');
    var label = resource.firstChild.nodeValue;
    resource.firstChild.nodeValue = altTxt;
    resource.setAttribute('title', label);
    toggleFlag(resource);
}

function switchLabelDirect(resource) {
    var altTxt = resource.getAttribute('title');
    var label = resource.firstChild.nodeValue;
    resource.firstChild.nodeValue = altTxt;
    resource.setAttribute('title', label);
    toggleFlag(resource);
}

function toggleFlag(resource) {
    if('none' == resource.getAttribute('name')) {
        resource.setAttribute('name', 'uri');
    } else {
        resource.setAttribute('name', 'none');
    }
}

function setShortLabel() {
    var resources = document.getElementsByName('uri');
    for(i = 0; i < resources.length; i++) {
        switchLabelDirect(resources.item(i));
    }
    setCookie("uriPref", 0, expire);
}

function setURILabel() {
    var resources = document.getElementsByName('none');
    for(i = 0; i < resources.length; i++) {
        switchLabelDirect(resources.item(i));
    }
    setCookie("uriPref", 1, expire);
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
