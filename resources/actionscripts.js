/*
 * Haiyang Xu, 15:00 Oct 4 2011 function to handle click on remove button
 */
function onRemoveClick(event, caller, $super, container, url, options) {
	event = event || window.event;
	var rmArg = '';
	var originalCaller = caller;
	var callerTagName;
	var callerId;
	var callerHash;
	while (caller.tagName != "ARTICLE" && caller.tagName != "HEADER") {
		caller = caller.parentNode;
		callerTagName = caller.tagName;
		callerId = caller.getAttribute("id");
		if (callerId != null
				&& checkTagAndId(callerTagName, callerId.split("_")[0])) {
			break;
		}

	}
	while (originalCaller.tagName != "ARTICLE"
			&& originalCaller.tagName != "HEADER") {
		originalCaller = originalCaller.parentNode;
		callerTagName = originalCaller.tagName;
		callerId = originalCaller.getAttribute("id");
		if (callerId != null
				&& checkTagAndId(callerTagName, callerId.split("_")[0])) {
			rmArg = callerId + '!' + rmArg;
			if (originalCaller.getAttribute("hash") != null
					&& originalCaller.getAttribute("hash") != ""
					&& callerId.split("_")[0] == "entry") {
				callerHash = originalCaller.getAttribute("hash");
				rmArg = rmArg + 'hash_' + callerHash + '!';
			}
		}

	}
	if (((rmArg.split("!").length == 3) && (rmArg.split("!")[1].split("_")[0] == "Title"))
			|| rmArg.split("!")[1].split("_")[0] == "Namespace"
			|| rmArg.split("!")[1].split("_")[0] == "Format"
			|| rmArg.split("!")[1].split("_")[0] == "Creator") {
		alert(rmArg.split("!")[1].split("_")[0] + " CANNOT be deleted!");
		return;
	}
	if (rmArg.split("!")[1].split("_")[1] == "Primary ID") {
		alert("Primary ID CANNOT be deleted!");
		return;
	}
	rmArg = 'part=' + rmArg;
	var confirmString = 'Are you sure to delete? ';
	if (callerHash != null && callerHash != "")
		confirmString = confirmString;
	var ans = confirm(confirmString);

	event.cancelBubble = true;
	event.returnValue = false;

	if (event.stopPropagation) {
		event.stopPropagation();
		event.preventDefault();
	}
	if (!ans) {
		return;
	}
	var tmp = caller;
	Effect.Fade(caller, {
		duration : 0.5,
		afterFinish : function() {
			tmp.parentNode.removeChild(tmp);
		}
	});
	new Ajax.Updater('block', '?cmd=remove&' + rmArg, {
		method : 'get',
		cache : false
	});
}

function insertAfter(referenceNode, newNode) {
	referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}

function onAddBlockClick() {
	// alert("Add Block");

	var newArticle = document.createElement("ARTICLE");

	new Ajax.Request('?cmd=add&part=new_block', {
		method : 'get',
		cache : false,
		// asynchronous : false,
		onSuccess : function(transport) {
			var res = transport.responseText || "no response";
			//alert(res);
			var lastid = res.split("_")[1] - 1;
			var lastarticle = document.getElementById("block_" + lastid);
			if(lastarticle==null){
				lastarticle = document.getElementById("header");
			}
			insertAfter(lastarticle, newArticle);

			newArticle.setAttribute("id", res);
			// alert(res+" " + lastarticle.innerHTML);
			new Ajax.Updater(res, 'st?part=' + res, {
				method : 'get'
			});

			Effect.ScrollTo(res, {
				duration : '1'
			});
			;

		}
	});
}

//Add new Memory
function onAddMemoryClick() {
	// alert("Add Memory");

	var newArticle = document.createElement("ARTICLE");

	new Ajax.Request('?cmd=add&part=new_memory', {
		method : 'get',
		cache : false,
		// asynchronous : false,
		
		onSuccess : function(transport) {
			var res = transport.responseText || "no response";
			location.reload(true);
		}
	});
}

// Called from the Overview
function onVisitClick() {
	var selectmenu = document.getElementById("select");
	var url = selectmenu.options[selectmenu.selectedIndex].value;
	window.location=url;
}

//Add new Memorywithout specified Path
function onAddMemoryWithoutPathClick() {
	// alert("Add Memory");

	var newArticle = document.createElement("ARTICLE");
	var name = prompt("Please enter the Name of the new new Memory","name");
	if(name != null){
		new Ajax.Request('?cmd=add&part=new_memory&name='+name, {
			method : 'get',
			cache : false,
			// asynchronous : false,
		
			onSuccess : function(transport) {
			
				var res = transport.responseText || "no response";
				window.location = '/web/'+name+'/';
			}
		});
	}

}

/*
 * Haiyang Xu, function to handle click on add button
 */
function onAddClick(event, caller, $super, container, url, options) {

	var addname = caller.getAttribute("addname");
	event = event || window.event;
	var addPath = '';
	var originalCaller = caller;
	var callerTagName;
	var callerId;
	while (caller.tagName != "ARTICLE") {
		caller = caller.parentNode;
		callerTagName = caller.tagName;
		callerId = caller.getAttribute("id");
		if (callerId != null
				&& checkTagAndId(callerTagName, callerId.split("_")[0])) {
			break;
		}

	}

	while (originalCaller.tagName != "ARTICLE") {
		originalCaller = originalCaller.parentNode;
		callerTagName = originalCaller.tagName;
		callerId = originalCaller.getAttribute("id");
		if (callerId != null
				&& checkTagAndId(callerTagName, callerId.split("_")[0])) {
			addPath = callerId + '!' + addPath;
		}

	}
	addPath = addPath.substring(0, addPath.length - 1);
	var addPathArray = addPath.split("!");
	var newForm;
	switch (addPathArray.length) {
	case 1:
		// alert("caption");
		// there is no the to-add item already. need to add a new table
		// entry
		addname = addname + "_" + addPathArray[0].split("_")[1];

		var temp = caller.childNodes[1].childNodes[1];
		var len = temp.childNodes.length;
		var last = caller.childNodes[1].childNodes[1].childNodes[1];
		var entryClass;
		if (last.getAttribute("class") == "even") {
			entryClass = "odd";
		} else
			entryClass = "even";
		newForm = buildNewEntryEditForm(caller.childNodes[1].childNodes[1],
				addname, entryClass, addPathArray);
		if (newForm != null) {
			new Effect.Highlight(newForm, {
				duration : 4
			});
			Effect.ScrollTo(newForm, {
				duration : '0.5'
			});
		}
		break;
	case 2:
		// alert("metadata " + addPathArray[0] + addPathArray[1]);
		// alert("caller = " + caller.innerHTML);
		switch (addPathArray[1].split("_")[0]) {
		case "Title":
			newForm = buildAddTitleEditForm(caller, addPathArray);
			break;

		case "Contributors":
			newForm = buildAddContributorEditForm(caller, addPathArray);
			break;
		case "Description":
			newForm = buildAddDescriptionEditForm(caller, addPathArray);
			break;
		case "Subject":
			newForm = buildAddSubjectEditForm(caller, addPathArray);
			break;

		}

		if (newForm != null) {
			new Effect.Highlight(newForm, {
				duration : 4
			});
			Effect.ScrollTo(newForm, {
				duration : '0.5'
			});
		}

		break;
	default:
		alert("unknown");
		break;

	}

	event.cancelBubble = true;
	event.returnValue = false;

	if (event.stopPropagation) {
		event.stopPropagation();
		event.preventDefault();
	}

}

/*
 * Haiyang Xu, function to handle click on multi-language select button.
 */
function onMultilangClick(caller, block, type) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=' + type + 'multilang!' + block
				+ '', {
			method : 'get',
			cache : false,
			onSuccess : function(transport) {
				var multilangsstr = transport.responseText || "no response";
				var multilangs = multilangsstr.split(",");

				for ( var i = 0; i < multilangs.length; i++) {
					var option = document.createElement("option");
					option.setAttribute("value", multilangs[i]);
					option.innerHTML = multilangs[i];
					caller.appendChild(option);
				}
			}
		});
	}
}

/*
 * Haiyang Xu, function to handle click on Mine Type select button.
 */
function onMinetypeClick(caller, block, select) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=mimetype!' + block + '', {
			method : 'get',
			cache : true,
			onSuccess : function(transport) {
				var multilangsstr = transport.responseText || "no response";
				var multilangs = multilangsstr.split(",");

				for ( var i = 0; i < multilangs.length; i++) {
					var option = document.createElement("option");
					option.setAttribute("value", multilangs[i]);
					option.innerHTML = multilangs[i];
					if (select == multilangs[i]) {
						option.setAttribute("selected", "selected");
					}
					caller.appendChild(option);
				}
			}
		});
	}
}

/*
 * Haiyang Xu, function to handle click on Payload Id Info key select button.
 */
function onPayloadIdInfoKeysClick(caller, block, select) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=payloadidinfokeys!' + block + '',
				{
					method : 'get',
					cache : true,
					onSuccess : function(transport) {
						var entitytypestr = transport.responseText
								|| "no response";
						var entitytypes = entitytypestr.split(",");

						for ( var i = 0; i < entitytypes.length; i++) {
							var option = document.createElement("option");
							option.setAttribute("value", entitytypes[i]);
							option.innerHTML = entitytypes[i];
							if (select == entitytypes[i]) {
								option.setAttribute("selected", "selected");
							}
							caller.appendChild(option);
						}
					}
				});
	}
}

/*
 * Haiyang Xu, function to handle click on Contributor Entity Type select
 * button.
 */
function onCtbrEtTpClick(caller, block, select) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=contributorEntityType!' + block
				+ '', {
			method : 'get',
			cache : true,
			onSuccess : function(transport) {
				var entitytypestr = transport.responseText || "no response";
				var entitytypes = entitytypestr.split(",");

				for ( var i = 0; i < entitytypes.length; i++) {
					var option = document.createElement("option");
					option.setAttribute("value", entitytypes[i]);
					option.innerHTML = entitytypes[i];
					if (select == entitytypes[i]) {
						option.setAttribute("selected", "selected");
					}
					caller.appendChild(option);
				}
			}
		});
	}
}

/*
 * Haiyang Xu, function to handle click on add new contributor edit form button.
 * Example of addPathArray: block_1,Contributors_1
 */
function buildAddContributorEditForm(caller, addPathArray) {
	var contributorTable = caller.ownerDocument.getElementById(addPathArray[1]);
	var entries = contributorTable.childNodes[1].childNodes[0].childNodes[0];
	var nentries = entries.childNodes.length;
	var classtype = entries.childNodes[nentries - 1].getAttribute("class");
	if (classtype == "odd")
		classtype = "even";
	else
		classtype = "odd";
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!" + addPathArray[1];

	var tr = document.createElement("tr");
	tr.setAttribute("class", classtype);
	tr.setAttribute("id", "entry_");
	tr.setAttribute("hash", hash);

	var entitytype = document.createElement("td");
	entitytype.innerHTML = "<select id=\"MultiLangSelect\" onclick=\"onCtbrEtTpClick(this, '"
			+ addPathArray[0]
			+ "')\" name=\"entitytype\">"
			+ "<option>Entity-Type</option>" + "</select>";
	var value = document.createElement("td");
	value.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ "30" + "\">";
	var date = document.createElement("td");
	var now = new Date();
	now = now.toJSON();
	now = now.replace(/-/g, "/");

	var datevalue = now.split("T")[0];
	var time = now.split("T")[1];
	datevalue = datevalue.substring(5) + "\/" + datevalue.substring(2, 4);

	var dateCellName = "date";
	var randomid = Math.random();
	date.innerHTML = "<div id=\"popupDateField"
			+ randomid
			+ "\" class=\"dateField\" style=\"margin-top: 0px\">"
			+ datevalue
			+ "</div>"
			+ "<input id=popupTimeField"
			+ randomid
			+ " class=\"timeField\" name=\"time\" style=\"margin-top: 0px\" type=\"text\" value=\""
			+ time.split(".")[0] + "\">" + "<input name=\"" + dateCellName
			+ "\" type=\"text\" value=\"" + datevalue
			+ "\" style=\"display: none;\">";

	var action = document.createElement("td");
	action.setAttribute("align", "right");
	var elemToRmStr = "this.parentNode.parentNode";
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);

	action.innerHTML = buildAddSaveFormButton(editArg, form.getAttribute("id"))
			+ buildCancelAddEntryButton(elemToRmStr);
	tr.appendChild(entitytype);
	tr.appendChild(value);
	tr.appendChild(date);
	tr.appendChild(action);
	entries.appendChild(tr);
	tr.appendChild(form);

	Calendar.setup({
		dateField : 'popupDateField' + randomid + '',
		triggerElement : 'popupDateField' + randomid + '',
		dateFormat : '%m/%d/%Y',
	});
	Protoplasm.use('timepicker');
	var picker = new Control.TimePicker('popupTimeField' + randomid + '', {
		format : "HH:mm"
	});
	return tr;
}

/*
 * Haiyang Xu, function to handle click on add new subject form button.
 */
function buildAddSubjectEditForm(caller, addPathArray) {
	var subTable = caller.ownerDocument.getElementById(addPathArray[1]);
	var entries = subTable.childNodes[1].childNodes[0].childNodes[0];
	var nentries = entries.childNodes.length;

	var classtype = entries.childNodes[nentries - 1].getAttribute("class");
	if (classtype == "odd")
		classtype = "even";
	else
		classtype = "odd";
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!" + addPathArray[1];
	var tr = document.createElement("tr");
	tr.setAttribute("class", classtype);
	tr.setAttribute("id", "entry_");
	tr.setAttribute("hash", hash);

	var subType = document.createElement("td");
	subType.innerHTML = "<select id=\"MultiLangSelect\" onchange=\"onSubjectTypeChange(this)\" onclick=\"onSubjectTypeSelectClick(this, '"
			+ addPathArray[0]
			+ "','"
			+ null
			+ "')\" name=\"subjecttype\">"
			+ "<option selected=\"selected\">Subject-Type</option>"
			+ "</select>";

	var subValueCell = document.createElement("td");
	var actionCell = document.createElement("td");

	var elemToRmStr = "this.parentNode.parentNode";

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);

	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAddEntryButton(elemToRmStr);

	tr.appendChild(subType);
	tr.appendChild(subValueCell);
	tr.appendChild(actionCell);
	tr.appendChild(form);

	entries.appendChild(tr);
	return tr;
}

/*
 * Haiyang Xu, function to handle click on add new description form button.
 */
function buildAddDescriptionEditForm(caller, addPathArray) {
	var desTable = caller.ownerDocument.getElementById(addPathArray[1]);
	var entries = desTable.childNodes[1].childNodes[0].childNodes[0];
	var nentries = entries.childNodes.length;

	var classtype = entries.childNodes[nentries - 1].getAttribute("class");
	if (classtype == "odd")
		classtype = "even";
	else
		classtype = "odd";
	var hash = addPathArray + Math.random();

	var editArg = addPathArray[0] + "!" + addPathArray[1];

	var tr = document.createElement("tr");
	tr.setAttribute("class", classtype);
	tr.setAttribute("id", "entry_");
	tr.setAttribute("hash", hash);

	var multiLang = document.createElement("td");

	multiLang.innerHTML = "<select id=\"MultiLangSelect\" onclick=\"onMultilangClick(this, '"
			+ addPathArray[0]
			+ "', 'des')\" name=\"multilangkey\">"
			+ "<option>Languages</option>" + "</select>";

	var value = document.createElement("td");
	value.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ "30" + "\">";

	var action = document.createElement("td");
	action.setAttribute("align", "right");
	var elemToRmStr = "this.parentNode.parentNode";

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);

	action.innerHTML = buildAddSaveFormButton(editArg, form.getAttribute("id"))
			+ buildCancelAddEntryButton(elemToRmStr);
	tr.appendChild(multiLang);
	tr.appendChild(value);
	tr.appendChild(action);
	entries.appendChild(tr);

	tr.appendChild(form);
	return tr;
}

/*
 * Haiyang Xu, function to handle click on add new title form button.
 */
function buildAddTitleEditForm(caller, addPathArray) {
	var titleTable = caller.ownerDocument.getElementById(addPathArray[1]);
	var entries = titleTable.childNodes[1].childNodes[0].childNodes[0];
	var nentries = entries.childNodes.length;

	var classtype = entries.childNodes[nentries - 1].getAttribute("class");
	if (classtype == "odd")
		classtype = "even";
	else
		classtype = "odd";
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!" + addPathArray[1];

	var tr = document.createElement("tr");
	tr.setAttribute("class", classtype);
	tr.setAttribute("id", "entry_");
	tr.setAttribute("hash", hash);

	var multiLang = document.createElement("td");

	multiLang.innerHTML = "<select id=\"MultiLangSelect\" onclick=\"onMultilangClick(this, '"
			+ addPathArray[0]
			+ "','title')\" name=\"multilangkey\">"
			+ "<option>Languages</option>" + "</select>";

	var value = document.createElement("td");
	value.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ "30" + "\">";

	var action = document.createElement("td");
	action.setAttribute("align", "right");
	var elemToRmStr = "this.parentNode.parentNode";

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);

	action.innerHTML = buildAddSaveFormButton(editArg, form.getAttribute("id"))
			+ buildCancelAddEntryButton(elemToRmStr);
	tr.appendChild(multiLang);
	tr.appendChild(value);
	tr.appendChild(action);
	entries.appendChild(tr);

	tr.appendChild(form);
	return tr;

}

/*
 * Haiyang Xu, function to handle click on add new entry form button. Example:
 * for block without contributors entries, this function will add a new edit
 * form of a new contributor entry
 */
function buildNewEntryEditForm(caller, addName, entryClass, addPathArray) {
	var newForm = caller.childNodes[1].cloneNode(true);
	var entryName = addName.split("_")[0];
	var blockId = addName.split("_")[1];

	newForm.childNodes[0].innerHTML = entryName;
	newForm.setAttribute("id", addName);
	newForm.setAttribute("hash", "");
	var tempelem = newForm.childNodes[1].childNodes[0].childNodes[0];

	switch (entryName) {
	case "Subject":
		tempelem.childNodes[0].childNodes[0].innerHTML = "Subject-Type";
		while (tempelem.childNodes.length > 2) {
			tempelem.removeChild(tempelem.lastChild);
		}
		newSubjectEditForm(tempelem.childNodes[1], addPathArray);
		newForm.childNodes[2].innerHTML = "";
		caller.parentNode.appendChild(newForm);
		return newForm;
		break;

	case "Contributor":
		tempelem.childNodes[0].childNodes[0].innerHTML = "Entity-Type";
		tempelem.childNodes[0].appendChild(tempelem.childNodes[0].childNodes[2]
				.cloneNode(true));
		tempelem.childNodes[0].childNodes[2].innerHTML = "Date";
		while (tempelem.childNodes.length > 2) {
			tempelem.removeChild(tempelem.lastChild);
		}
		newForm.childNodes[2].innerHTML = "";
		var datetimeid = newContributorsEditForm(tempelem.childNodes[1],
				addPathArray);
		// alert(datetimeid);

		caller.parentNode.appendChild(newForm);
		addDateTimePicker(datetimeid.split("&")[0], datetimeid.split("&")[1]);
		return newForm;
		break;

	case "Description":
		tempelem.childNodes[0].childNodes[0].innerHTML = "Key";
		while (tempelem.childNodes.length > 2) {
			tempelem.removeChild(tempelem.lastChild);
		}
		newDescriptionEditForm(tempelem.childNodes[1], addPathArray);
		newForm.childNodes[2].innerHTML = "";
		caller.parentNode.appendChild(newForm);
		return newForm;

		break;

	case "Format":
		// tempelem.childNodes[0].
		break;

	case "Type":
		newForm.childNodes[1].innerHTML = "";
		newForm.childNodes[2].innerHTML = "";

		// alert(newForm.innerHTML);
		newTypeEditForm(newForm, addPathArray);
		caller.parentNode.appendChild(newForm);
		return newForm;
		break;

	case "Link":
		newForm.childNodes[1].innerHTML = "";
		newForm.childNodes[2].innerHTML = "";

		// alert(newForm.innerHTML);
		newLinkEditForm(newForm, addPathArray);
		caller.parentNode.appendChild(newForm);
		return newForm;
		break;
	case "Payload":
		newForm.childNodes[1].innerHTML = "";
		newForm.childNodes[2].innerHTML = "";

		// alert(newForm.innerHTML);
		newPayloadEditForm(newForm, addPathArray);
		caller.parentNode.appendChild(newForm);
		return newForm;
		break;
	default:
		break;

	}
	return null;

}

function newPayloadEditForm(caller, addPathArray) {

	var editArg = addPathArray[0] + "!Payload_" + addPathArray[0].split("_")[1];
	var hash = Math.random();
	caller.setAttribute("id", "Payload_" + addPathArray[0].split("_")[1]);
	caller.setAttribute("hash", hash);

	var valueCell = caller.childNodes[1];
	valueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ 100 + "\">";

	var actionCell = caller.childNodes[2];
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);
	caller.appendChild(form);

	var elemToRmStr = "this.parentNode.parentNode";
	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAdditionButton(elemToRmStr);
	modifyMetaPayloadUploadCell(caller, editArg + "!hash_" + hash);
	actionCell.innerHTML = buildCancelAddEntryButton(elemToRmStr);
}

/*
 * Haiyang Xu, function to handle click on add new type form button.
 */
function newTypeEditForm(caller, addPathArray) {
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!Type_" + addPathArray[0].split("_")[1];
	caller.setAttribute("id", "Type_" + addPathArray[0].split("_")[1]);
	caller.setAttribute("hash", hash);
	var valueCell = caller.childNodes[1];
	valueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ 100 + "\">";

	var actionCell = caller.childNodes[2];
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);
	caller.appendChild(form);

	var elemToRmStr = "this.parentNode.parentNode";
	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAdditionButton(elemToRmStr);
}

function newLinkEditForm(caller, addPathArray) {
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!Link_" + addPathArray[0].split("_")[1];
	caller.setAttribute("id", "Link_" + addPathArray[0].split("_")[1]);
	caller.setAttribute("hash", hash);
	var valueCell = caller.childNodes[1];
	valueCell.innerHTML = "<input name=\"value\" type=\"text\" oninput=\"onLinkChange(event);\" value=\"http://somesamplelink\" size=\""
			+ 100 + "\">";

	var actionCell = caller.childNodes[2];
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);
	caller.appendChild(form);

	var elemToRmStr = "this.parentNode.parentNode";
	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAddEntryButton(elemToRmStr);

}

function newContributorsEditForm(caller, addPathArray) {
	// alert(caller.innerHTML);
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!Contributors_"
			+ addPathArray[0].split("_")[1];

	caller.setAttribute("id", "entry_");
	caller.setAttribute("hash", hash);

	// since the model table has only 3 children so we will copy the last child
	// and append it
	caller.appendChild(caller.childNodes[2].cloneNode(true));
	var modelElement = caller.childNodes[2].cloneNode(true);

	var entityTypeCell = document.createElement("td");
	entityTypeCell.innerHTML = "<select name=\"entitytype\" id=\"EntityTypeSelect\" "
			+ "onClick=\"onCtbrEtTpClick(this,'"
			+ addPathArray[0]
			+ "','"
			+ null + "');\">" + "<option>Entity-Type</option>" + "</select>";

	var valueCell = document.createElement("td");
	valueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ 50 + "\">";
	var dateCell = document.createElement("td");
	var now = new Date();
	now = now.toJSON();
	now = now.replace(/-/g, "/");

	var date = now.split("T")[0];
	var time = now.split("T")[1];
	date = date.substring(5) + "\/" + date.substring(2, 4);

	var dateCellName = "date";
	/*
	 * dateCell.innerHTML = "<input name=\"" + "date" + "\" type=\"text\"
	 * value=\"" + now.split("T")[0] + "\" size=\"10\"
	 * onclick=\"displayCalendar(this.parentNode.childNodes[0],'yyyy-mm-dd',this,
	 * true)\">" + "<input name=\"time\" type=\"text\" value=\"" +
	 * now.split("T")[1].split(".")[0] + "\" size=\"8\">";
	 */

	var newrandomid = Math.random();
	// alert(newrandomid);
	dateCell.innerHTML = "<div id=\"popupDateField"
			+ newrandomid
			+ "\" class=\"dateField\" style=\"margin-top: 0px\">"
			+ date
			+ "</div>"
			+ "<input id=popupTimeField"
			+ newrandomid
			+ " class=\"timeField\" name=\"time\" style=\"margin-top: 0px\" type=\"text\" value=\""
			+ time.split(".")[0] + "\">" + "<input name=\"" + dateCellName
			+ "\" type=\"text\" value=\"" + date
			+ "\" style=\"display: none;\">";

	var actionCell = document.createElement("td");
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);

	var elemToRmStr = "this.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode";
	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAdditionButton(elemToRmStr);

	removeAllChildren(caller);
	caller.appendChild(entityTypeCell);
	caller.appendChild(valueCell);
	caller.appendChild(dateCell);
	caller.appendChild(actionCell);
	caller.appendChild(form);
	// alert(dateCell.childNodes[0].getAttribute("id"));
	// var el = document.getElementById("popupDateField"+newrandomid);
	// alert(el.innerHTML);
	/*
	 * Calendar.setup({ dateField : 'popupDateField'+newrandomid+'',
	 * triggerElement : 'popupDateField'+newrandomid+'', dateFormat : '%m/%d/%Y' ,
	 * }); // Protoplasm.use('timepicker').transform('input.timeField');
	 * Protoplasm.use('timepicker'); var picker = new
	 * Control.TimePicker('popupTimeField'+newrandomid+'', { format : "HH:mm"
	 * });
	 */
	var ret = 'popupDateField' + newrandomid + '&' + 'popupTimeField'
			+ newrandomid;
	return ret;
}

function addDateTimePicker(datefield, timefield) {
	Calendar.setup({
		dateField : datefield,
		triggerElement : datefield,
		dateFormat : '%m/%d/%Y',
	});

	// Protoplasm.use('timepicker').transform('input.timeField');
	Protoplasm.use('timepicker');
	var picker = new Control.TimePicker(timefield, {
		format : "HH:mm"
	});
}

/*
 * Haiyang Xu, function to handle click on add new description form button.
 */
function newDescriptionEditForm(caller, addPathArray) {
	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!Description_"
			+ addPathArray[0].split("_")[1];

	caller.setAttribute("id", "entry_");
	caller.setAttribute("hash", hash);

	var keyCell = caller.childNodes[0];
	keyCell.innerHTML = "<select id=\"MultiLangSelect\" onclick=\"onMultilangClick(this, '"
			+ addPathArray[0]
			+ "', 'des')\" name=\"multilangkey\">"
			+ "<option>Languages</option>" + "</select>";

	var valueCell = caller.childNodes[1];
	valueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ 50 + "\">";

	var actionCell = caller.childNodes[caller.childNodes.length - 1];

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);
	caller.appendChild(form);

	var elemToRmStr = "this.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode";
	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAdditionButton(elemToRmStr);

}

/*
 * Haiyang Xu, function to handle click on add new subject form button.
 */
function newSubjectEditForm(caller, addPathArray) {

	var hash = addPathArray + Math.random();
	var editArg = addPathArray[0] + "!Subject_" + addPathArray[0].split("_")[1];

	caller.setAttribute("id", "entry_");
	caller.setAttribute("hash", hash);

	var subjectTypeCell = caller.childNodes[0];
	var subTypes = getSubjectTypeList();
	var options = "";
	subjectTypeCell.innerHTML = "<select id=\"SubjectTypeSelect\" "
			+ "name=\"subjecttype\" "
			+ "onclick=\"onSubjectTypeSelectClick(this," + "'"
			+ editArg.split("!")[0] + "'" + ",'" + "" + "')\" "
			+ "onChange=\"onSubjectTypeChange(this);\">" + options
			+ "</select>";
	;
	var originalValueCell = caller.childNodes[1];
	var originalValue = getValueCellText(originalValueCell);
	originalValueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\"\" size=\""
			+ originalValue.length + "\">";
	var actionCell = caller.childNodes[caller.childNodes.length - 1];

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', hash);
	caller.appendChild(form);

	var elemToRmStr = "this.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode";

	actionCell.innerHTML = buildAddSaveFormButton(editArg, form
			.getAttribute("id"))
			+ buildCancelAdditionButton(elemToRmStr);

}

/*
 * Haiyang Xu, function to handle click on edit header primary id button.
 */
function modifyHeaderPrimaryIDCell(caller, editArg) {
	addCache(caller.getAttribute("hash"), caller.innerHTML);
	editArg = editArg + "!hash_" + caller.getAttribute("hash");
	var formid = caller.getAttribute("hash");
	var originalValueCell = caller.childNodes[1];
	var originalValue = originalValueCell.childNodes[0].innerHTML;
	originalValueCell.innerHTML = "<form id=\"" + formid + "\">"
			+ "<input name=\"value\" type=\"text\" value=\"" + originalValue
			+ "\" size=\"" + originalValue.length + "\">" + "</form>";
	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveFormButton(editArg, formid)
			+ buildCancelButton();
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}
}

/*
 * Haiyang Xu, function to handle click on edit button.
 */
function onEditClick(event, caller, $super, container, url, options) {
	event = event || window.event;
	var editArg = '';
	var originalCaller = caller;
	var callerTagName;
	var callerId;
	var hash = "";
	// find which metadata table this entry belongs to.
	while ((caller.tagName != "ARTICLE") && (caller.tagName != "HEADER")) {
		caller = caller.parentNode;
		callerTagName = caller.tagName;
		callerId = caller.getAttribute("id");
		if (callerId != null
				&& checkTagAndId(callerTagName, callerId.split("_")[0])) {
			break;
		}
	}
	// alert("caller = " + caller.tagName + ", " + caller.getAttribute("id"));
	// find the edition path. block_# -> metadata -> entry_*
	while ((originalCaller.tagName != "ARTICLE")
			&& (originalCaller.tagName != "HEADER")) {
		originalCaller = originalCaller.parentNode;
		callerTagName = originalCaller.tagName;
		callerId = originalCaller.getAttribute("id");
		if (callerId != null
				&& checkTagAndId(callerTagName, callerId.split("_")[0])) {
			editArg = callerId + '!' + editArg;

		}
	}
	// alert(editArg);
	// alert(originalCaller.innerHTML);
	var editPart = originalCaller.tagName;
	editArg = editArg.substring(0, editArg.length - 1);
	// alert("edit=" + editArg);
	var hash = caller.getAttribute("hash");
	var metaDataToEdit = '';
	var metaEntryToEdit = '';
	var path = editArg.split("!");
	if (editPart == "ARTICLE") {
		switch (path.length) {
		case 1:
			// alert("length = 1");
			break;
		case 2:
			// edit a metadata
			// alert("length = 2" + editArg);
			metaDataToEdit = path[1].split("_")[0];
			editEntry(caller, metaDataToEdit, editArg);
			break;
		case 3:
			// edit a metadata entry
			// alert("length = 3");
			metaDataToEdit = path[1].split("_")[0];
			metaEntryToEdit = path[2].split("_")[1];
			editEntry(caller, metaDataToEdit, editArg);
			break;
		default:
			alert("unknown " + rmArg.split("!").length + rmArg.split("!")[2]);
			break;
		}
	} else if (editPart == "HEADER") {
		modifyHeaderPrimaryIDCell(caller, editArg);

	}
	// alert("metaDataToEdit="+metaDataToEdit + "
	// metaEntryToEdit="+metaEntryToEdit);
	event.cancelBubble = true;
	event.returnValue = false;

	if (event.stopPropagation) {
		event.stopPropagation();
		event.preventDefault();
	}

	var tmp = caller;
	// Effect.Fade(caller, {afterFinish: function() {
	// tmp.parentNode.removeChild(tmp); }});
	/*
	 * Modalbox.show("../resources/add_Subject.html", { title : "Add " +
	 * getAddTerm(container) });
	 */
}

/*
 * Haiyang Xu, function modify table cell consisting of Value cell and action
 * cell
 */
function modifyValueActionCell(caller, editArg) {

	var valueCell = caller.childNodes[1];
	var originalValue = valueCell.innerHTML;
	var formid = caller.getAttribute("hash");
	// alert(valueCell.offsetWidth);

	valueCell.innerHTML = /*
							 * "<form id=\"" + formid + "\">
							 */"<input name=\"value\" type=\"text\" value=\"" + originalValue
			+ "\" width=\"" + valueCell.offsetWidth + "\">";
	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveFormButton(editArg, formid)
			+ buildCancelButton()/* + "</form>" */;

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.getAttribute("hash"));
	caller.appendChild(form);

	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}

}

function getEntityTypes() {
	var ret = new Array("email", "duns");
	return ret;
}

function onDateInputFieldChange(caller, datefield) {
	document.getElementById(datefield).innerHTML = caller.getAttribute("value");
}

/*
 * Haiyang Xu, function modify table cell consisting of Value cell, Date cell
 * and action cell
 */
function modifyValueDateActionCell(caller, editArg) {
	var entityTypeCell = caller.childNodes[0];
	var originalEnityType = entityTypeCell.innerHTML;
	var enityTypes = getEntityTypes();
	var options = "";
	for ( var i = 0; i < enityTypes.length; i++) {
		var opt = "<option value=\"" + enityTypes[i] + "\"";
		if (enityTypes[i] != null && enityTypes[i] == originalEnityType) {
			opt = opt + " selected=\"selected\"";
			opt = opt + ">" + enityTypes[i] + "</option>";
			options = options + opt;
			break;
		}
	}
	entityTypeCell.innerHTML = "<select name=\"entitytype\" id=\"EntityTypeSelect\" "
			+ "onClick=\"onCtbrEtTpClick(this,'"
			+ editArg.split("!")[0]
			+ "','" + originalEnityType + "');\">" + options + "</select>";
	;

	/*
	 * 
	 */
	var valueCell = caller.childNodes[1];
	var originalValue = valueCell.innerHTML;
	valueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\""
			+ originalValue + "\" size=\"" + originalValue.length + "\">";
	var dateCell = caller.childNodes[2];
	var originalDateValue = dateCell.innerHTML.split(" ")[0]
	var originalTimeValue = dateCell.innerHTML.split(" ")[1] + " "
			+ dateCell.innerHTML.split(" ")[2];
	var dateCellName = "date";
	// + dateCellCount++;
	/*
	 * dateCell.innerHTML = "<input name=\"" + dateCellName + "\" type=\"text\"
	 * value=\"" + originalDateValue + "\" size=\"10\"
	 * onclick=\"displayCalendar(this.parentNode.childNodes[0],'m/dd/yy',this,
	 * true)\">" + "<input name=\"time\" type=\"text\" value=\"" +
	 * originalTimeValue + "\" size=\"8\">";
	 */

	var randomid = Math.random();
	var popupDateFieldId = "popupDateField" + randomid;
	dateCell.innerHTML = "<input id=\"popupDateField"
			+ randomid
			+ "\" name=\"date\" value=\""
			+ originalDateValue
			+ "\" class=\"dateField\" style=\"margin-top: 0px\">"
			+ "</input>"
			+ "<input id=popupTimeField"
			+ randomid
			+ " class=\"timeField\" name=\"time\" style=\"margin-top: 0px\" type=\"text\" value=\""
			+ originalTimeValue + "\">";
	Calendar.setup({
		dateField : 'popupDateField' + randomid + '',
		triggerElement : 'popupDateField' + randomid + '',
		dateFormat : '%m/%d/%Y',
	});
	// Protoplasm.use('timepicker').transform('input.timeField');
	Protoplasm.use('timepicker');
	var picker = new Control.TimePicker('popupTimeField' + randomid + '', {
		format : "HH:mm"
	});
	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveFormButton(editArg, caller
			.getAttribute("hash"))
			+ buildCancelButton();
	// var callerclone = caller.cloneNode(true);
	// removeAllChildren(caller);
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.getAttribute("hash"));
	// form.appendChild(callerclone);
	caller.appendChild(form);
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}

}

/*
 * Haiyang Xu, Function to build save form button
 */
function buildSaveFormButton(editArg, formid) {
	var metaType = editArg.split("!")[1].split("_")[0];
	return buildButton("Save", "Save change", "onSaveFormClick(this, '"
			+ editArg + "', '" + metaType + "', '" + formid + "'" + ",'edit'"
			+ ")");
}

/*
 * Haiyang Xu, Function to build save meta payload button
 */
function buildSaveMetaPayloadButton(editArg) {
	return buildButton("Save", "Save change", "onMetaPayloadSaveClick(this,'"
			+ editArg + "')");
}

/*
 * Haiyang Xu, Function to build add/save form button
 */
function buildAddSaveFormButton(addArg, formid) {
	var metaType = addArg.split("!")[1].split("_")[0];
	return buildButton("Save", "Save change", "onSaveFormClick(this, '"
			+ addArg + "', '" + metaType + "', '" + formid + "'" + ", 'add'"
			+ ")");
}

/*
 * Haiyang Xu, Function to build new/save form button
 */
function buildNewSaveFormButton(addArg, formid, metaType) {
	return buildButton("Save", "Save change", "onSaveFormClick(this, '"
			+ addArg + "', '" + metaType + "', '" + formid + "'" + ", 'add'"
			+ ")");
}

/*
 * Haiyang Xu, Function to handle the click on same form button
 */
function onSaveFormClick(caller, editArg, metaType, formid, addoredit) {
	var form = $(formid);
	var selects = caller.parentNode.parentNode.getElementsByTagName('SELECT');
	for ( var i = 0; i < selects.length; i++)
		form.appendChild(selects[0]);

	var inputs = caller.parentNode.parentNode.getElementsByTagName('INPUT');
	for ( var i = 0; i < inputs.length; i++)
		form.appendChild(inputs[0]);

	var serialform = form.serialize();
	var serialformarray = serialform.split("&");
	var formmap = new Array();
	for ( var i = 0; i < serialformarray.length; i++) {
		var temp = serialformarray[i].split("=");
		formmap[temp[0]] = temp[1];
	}
	var odd = caller.parentNode.parentNode.getAttribute("class");
	if (odd != "odd" | "even")
		odd = "odd";

	var tempelem = caller.parentNode.parentNode;
	var newid, id;
	newid = tempelem.getAttribute("id");
	
	if (formmap["multilangkey"]) {
		id = tempelem.getAttribute("id");
		newid = id + formmap["multilangkey"];
		// tempelem.setAttribute("id", newid);
		tempelem.setAttribute("id", tempelem.getAttribute("hash"));
	} else if (formmap["subjecttype"]) {
		id = tempelem.getAttribute("id");
		if (id == "entry_") {
			newid = id + formmap["subjecttype"];
			tempelem.setAttribute("id", newid);
		}
	} else if (formmap["entitytype"]) {
		id = tempelem.getAttribute("id");
		if (id == "entry_") {
			newid = id + formmap["entitytype"];
			tempelem.setAttribute("id", newid);
			// tempelem.setAttribute("id", tempelem);

		} else {
			newid = "entry_" + formmap["entitytype"];

		}
		tempelem.setAttribute("id", tempelem.getAttribute("hash"));
	} else if (formmap["minetype"]) {

		id = tempelem.getAttribute("id");
		if (id == "entry_") {
			newid = id + formmap["minetype"];
			tempelem.setAttribute("id", newid);
			// tempelem.setAttribute("id", tempelem);

		} else {
			newid = "entry_" + formmap["minetype"];
			var idslash = newid.indexOf("%");
			newid = newid.substring(0, idslash) + "/"
					+ newid.substring(idslash + 3);

		}
		tempelem.setAttribute("id", tempelem.getAttribute("hash"));
	}
	tempelem.setAttribute("id", tempelem.getAttribute("hash"));
	var oldhash = tempelem.getAttribute("hash");

	new Ajax.Updater(caller.parentNode.parentNode.getAttribute("id"), '?cmd='
			+ addoredit + '&part=' + editArg + '!' + odd + '&' + serialform, {
		method : 'post',
		cache : false,
		asynchronous : false,
		onSuccess : function(transport) {
			var response = transport.responseText || "no response text";
			var index = response.indexOf("hash");
			var hash = response.substring(index + 6, index + 64 + 6);
			caller.parentNode.parentNode.setAttribute("hash", hash);
		}
	});
	var elem = document.getElementById(oldhash);
	elem.setAttribute("id", newid);

}

function checkSchema(schema) {
	return validateURL(decode(schema));
}

function validateURL(textval) {
	var urlregex = new RegExp(
			"^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$");
	return urlregex.test(textval);
}

function decode(str) {
	return unescape(str.replace(/\+/g, " "));
}

/*
 * Haiyang Xu, Function to remove all children of the caller
 */
function removeAllChildren(caller) {
	if (caller.hasChildNodes()) {
		while (caller.childNodes.length >= 1) {
			caller.removeChild(caller.firstChild);
		}
	}
}
var highlight = null;
var shake = null;

function onFormatSchemaChange(event) {

	if (!checkSchema(event.target.value)) {
		var savebtn = event.target.parentNode.parentNode.childNodes[3].childNodes[0];
		var saveclick = savebtn.getAttribute("onclick");
		savebtn
				.setAttribute(
						"onclick",
						"if(!schemaFieldCheck(this.parentNode.parentNode.childNodes[1].childNodes[0])) return;"
								+ " else{" + saveclick + "}");
		highlight = new Effect.Highlight(event.target, {
			duration : 3,

		});
		shake = new Effect.Shake(event.target, {
			duration : 0.5,
			distance : 1
		});
	} else {

		highlight.cancel();
		shake.cancel();
	}
}

function schemaFieldCheck(caller) {
	if (!checkSchema(caller.value)) {
		highlight = new Effect.Highlight(caller, {
			duration : 3,

		});
		shake = new Effect.Shake(caller, {
			duration : 0.5,
			distance : 1
		});
		return false;
	} else {
		highlight.cancel();
		shake.cancel();
		return true;
	}
}

/*
 * Haiyang Xu, Function to modify the format cell to make it editable
 */
function modifyFormatCell(caller, editArg) {
	var mineTypeCell = caller.childNodes[0];
	var originalMineType = mineTypeCell.innerHTML;
	var options;
	var originalSchema;
	var originalEncrypt;
	var schemaCell;
	var encryptCell;
	var opt = document.createElement("option");
	opt.setAttribute("value", originalMineType);
	opt.setAttribute("selected", "selected");
	opt.innerHTML = originalMineType
	if (caller.childNodes.length == 4) {
		originalSchema = caller.childNodes[1].childNodes[0].innerHTML;
		originalEncrypt = caller.childNodes[2].innerHTML;
		schemaCell = caller.childNodes[1];
		encryptCell = caller.childNodes[2];
		schemaCell.innerHTML = "<input type=\"text\" name=\"schema\" oninput=\"onFormatSchemaChange(event)\" value=\""
				+ originalSchema + "\">";
		encryptCell.innerHTML = "<input type=\"text\" name=\"encryption\" value=\""
				+ originalEncrypt + "\">";
	}
	mineTypeCell.innerHTML = "<select id=\"FormatSelect\" name=\"minetype\" "
			+ "onClick=\"onMinetypeClick(this,'" + editArg.split("!")[0]
			+ "','" + originalMineType + "')\" "
			+ "onChange=\"onFormatTypeChange(this);\"></select>";
	mineTypeCell.childNodes[0].appendChild(opt);
	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveFormButton(editArg, caller
			.getAttribute("hash"))
			+ buildCancelButton();

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.getAttribute("hash"));
	// form.appendChild(callerclone);
	caller.appendChild(form);
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}
}

/*
 * Haiyang Xu, Function monitoring the change of format type
 */
function onFormatTypeChange(caller) {
	var selectedText = caller.options[caller.selectedIndex].text;
	caller.parentNode.parentNode
			.removeChild(caller.parentNode.parentNode.lastChild);
	if (selectedText == "application/xml") {
		var tableCols = caller.parentNode.parentNode.parentNode.childNodes[0];
		var action = tableCols.childNodes[tableCols.childNodes.length - 1]
				.cloneNode(true);
		tableCols
				.removeChild(tableCols.childNodes[tableCols.childNodes.length - 1]);
		var schema = action.cloneNode(true);
		var encrypt = action.cloneNode(true);
		schema.setAttribute("align", "left");
		encrypt.setAttribute("align", "left");
		schema.innerHTML = "Schema";
		encrypt.innerHTML = "encryption";
		tableCols.appendChild(schema);
		tableCols.appendChild(encrypt);
		tableCols.appendChild(action);
		tableCols = caller.parentNode.parentNode.parentNode.childNodes[1];
		action = tableCols.childNodes[tableCols.childNodes.length - 1]
				.cloneNode(true);
		tableCols
				.removeChild(tableCols.childNodes[tableCols.childNodes.length - 1]);
		schema = action.cloneNode(true);
		encrypt = action.cloneNode(true);
		schema.setAttribute("align", "left");
		encrypt.setAttribute("align", "left");
		schema.innerHTML = "<input type=\"text\" name=\"schema\" value=\"\">";
		encrypt.innerHTML = "<input type=\"text\" name=\"encryption\" value=\"\">";
		tableCols.appendChild(schema);
		tableCols.appendChild(encrypt);
		tableCols.appendChild(action);

	} else if (selectedText == "text/plain" || selectedText == "video/webm"
			|| selectedText == "image/png" || selectedText == "audio/mpeg") {
		// alert(caller.parentNode.parentNode.parentNode.innerHTML);
		var tableCols = caller.parentNode.parentNode.parentNode.childNodes[0];

		var schema = tableCols.childNodes[1];
		var encrypt = tableCols.childNodes[2];
		if (schema != null && encrypt != null) {
			tableCols.removeChild(schema);

			tableCols.removeChild(encrypt);
			tableCols = caller.parentNode.parentNode.parentNode.childNodes[1];
			schema = tableCols.childNodes[1];
			encrypt = tableCols.childNodes[2];
			tableCols.removeChild(schema);
			tableCols.removeChild(encrypt);
		}

	}
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.parentNode.parentNode.getAttribute("hash"));
	// form.appendChild(callerclone);
	caller.parentNode.parentNode.appendChild(form);

}

function getSubjectTypeList() {
	var ret = new Array("Ontology", "Text");
	return ret;

}

/*
 * Haiyang Xu, function to handle the selecting of a subject type
 */
function onSubjectTypeSelectClick(caller, block, select) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=subjecttype!' + block + '', {
			method : 'get',
			cache : true,
			onSuccess : function(transport) {
				var entitytypestr = transport.responseText || "no response";
				var entitytypes = entitytypestr.split(",");

				for ( var i = 0; i < entitytypes.length; i++) {
					var option = document.createElement("option");
					option.setAttribute("value", entitytypes[i]);
					option.innerHTML = entitytypes[i];
					if (select == entitytypes[i]) {
						option.setAttribute("selected", "selected");
					}

					caller.appendChild(option);
					// alert(caller.options[caller.selectedIndex].text);
				}
			}
		});
	}

}

function onSubjectChildDeleteClick(caller) {
	var bros = caller.parentNode.childNodes.length;
	var childtoappend = null;
	if (caller.parentNode.childNodes[bros - 1].tagName == "UL"
			&& caller.parentNode.childNodes[bros - 1].innerHTML != "") {
		var temp = caller.parentNode.childNodes[bros - 1].childNodes[0]
				.cloneNode(true);
		childtoappend = temp;
	}
	var todel = caller.parentNode;
	var tobeadd = todel.parentNode;
	todel.parentNode.removeChild(todel);
	if (childtoappend != null)
		tobeadd.appendChild(childtoappend);

}

function onSubjectChildAddClick(caller) {
	var bros = caller.parentNode.childNodes.length;
	var parentLI = caller.parentNode;

	var ul = document.createElement("ul");
	var li = document.createElement("li");
	var addsub = document.createElement("img");
	addsub.setAttribute("class", "iaddsub");
	addsub.setAttribute("src",
			"../resources/images/subjectimg/add_sub-album.gif");
	addsub.setAttribute("onclick", "onSubjectChildAddClick(this)");
	li.appendChild(addsub);
	var input = document.createElement("input");
	input.setAttribute("name", "value");
	input.setAttribute("type", "text");
	li.appendChild(input);
	var del = document.createElement("img");
	del.setAttribute("class", "idel");
	del.setAttribute("src", "../resources/images/subjectimg/delete_album.gif")
	del.setAttribute("onclick", "onSubjectChildDeleteClick(this)");
	li.appendChild(del);
	ul.appendChild(li);

	if (parentLI.childNodes[bros - 1].tagName == "UL") {
		li.appendChild(parentLI.childNodes[bros - 1].cloneNode(true));
		parentLI.removeChild(parentLI.childNodes[bros - 1]);
	}

	parentLI.appendChild(ul);
}

function modifySubjectCell(caller, editArg) {
	var subjectTypeCell = caller.childNodes[0];
	var originalSubjectType = subjectTypeCell.innerHTML;
	var subTypes = getSubjectTypeList();
	var options = "";
	options = "<option value=\"" + originalSubjectType
			+ "\" selected=\"selected\">" + originalSubjectType + "</option>";
	subjectTypeCell.innerHTML = "<select id=\"SubjectTypeSelect\" "
			+ "name=\"subjecttype\" "
			+ "onclick=\"onSubjectTypeSelectClick(this," + "'"
			+ editArg.split("!")[0] + "'" + ",'" + originalSubjectType
			+ "')\" " + "onChange=\"onSubjectTypeChange(this);\">" + options
			+ "</select>";
	;
	var originalValueCell = caller.childNodes[1];
	var originalValue = getValueCellText(originalValueCell);
	originalValueCell.innerHTML = "<input name=\"value\" type=\"text\" value=\""
			+ originalValue + "\" size=\"" + originalValue.length + "\">";
	originalValueCell.innerHTML = "<ul><li>hello</li><ul><li>hello1</li></ul></ul>";

	var myajax = new Ajax.Request(
			'?cmd=download&part=subjectvalue!' + caller.getAttribute("hash")
					+ '!' + editArg.split("!")[0] + '',
			{
				method : 'get',
				onSuccess : function(transport) {
					var valuechain = transport.responseText.split(",");
					var oricell = originalValueCell;
					oricell.innerHTML = "";
					if (originalSubjectType == "Text") {
						for ( var i = 0; i < valuechain.length; i++) {
							var ul = document.createElement("ul");
							var li = document.createElement("li");

							var addsub = document.createElement("img");
							addsub.setAttribute("class", "iaddsub");
							addsub
									.setAttribute("src",
											"../resources/images/subjectimg/add_sub-album.gif");
							addsub.setAttribute("onclick",
									"onSubjectChildAddClick(this)");
							li.appendChild(addsub);

							var input = document.createElement("input");
							input.setAttribute("name", "value");
							input.setAttribute("type", "text");
							input.setAttribute("value", valuechain[i]);
							input
									.setAttribute(
											"size",
											valuechain[i].length > 20 ? valuechain[i].length
													: 20);
							li.appendChild(input);

							if (i > 0) {
								var del = document.createElement("img");
								del.setAttribute("class", "idel");
								del
										.setAttribute("src",
												"../resources/images/subjectimg/delete_album.gif")
								del.setAttribute("onclick",
										"onSubjectChildDeleteClick(this)");
								li.appendChild(del);
							}
							ul.appendChild(li);
							oricell.appendChild(ul);
							oricell = li;
						}
					} else {
						oricell.innerHTML = "<input type=\"text\" name=\"value\" "
								+ "value=\""
								+ valuechain[0]
								+ "\" size=\""
								+ (valuechain[0].length > 20 ? valuechain[0].length
										: 20) + "\">"
					}
				}
			});

	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.getAttribute("hash"));
	actionCell.innerHTML = buildSaveFormButton(editArg, form.getAttribute("id"))
			+ buildCancelButton();

	caller.appendChild(form);
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}
}

// extract value from a plain text tag or a href tag
// <a href="">...</a> or ...
function getValueCellText(valueCell) {
	var ret;
	if (valueCell.childNodes[0].tagName == "A") {
		ret = valueCell.childNodes[0].getAttribute("href");
	} else {
		ret = valueCell.innerHTML;
	}
	return ret;
}

function onSubjectTypeChange(caller) {

	// alert(caller.innerHTML);

	var oriVal = caller.options[caller.selectedIndex].text;
	// alert(oriVal);
	var oricell = caller.parentNode.parentNode.childNodes[1];
	if (oriVal == "Text") {
		var ul = document.createElement("ul");
		var li = document.createElement("li");

		var addsub = document.createElement("img");
		addsub.setAttribute("class", "iaddsub");
		addsub.setAttribute("src",
				"../resources/images/subjectimg/add_sub-album.gif");
		addsub.setAttribute("onclick", "onSubjectChildAddClick(this)");
		li.appendChild(addsub);

		var input = document.createElement("input");
		input.setAttribute("name", "value");
		input.setAttribute("type", "text");
		input.setAttribute("size", 20);
		li.appendChild(input);

		ul.appendChild(li);
		oricell.innerHTML = "";
		oricell.appendChild(ul);
	} else if (oriVal == "Ontology") {
		oricell.innerHTML = "<input type=\"text\" name=\"value\" "
				+ "\" size=\"" + "20" + "\">"
	} else {
		alert("haha")
	}
}

function modifyMetaDataCell(caller, editArg) {
	var originalValueCell = caller.childNodes[1];
	var originalValue = originalValueCell.childNodes[0].innerHTML;
	var formid = caller.getAttribute("hash");
	// alert(formid);
	originalValueCell.innerHTML = "<form id=\"" + formid + "\">"
			+ "<input name=\"value\" type=\"text\" value=\"" + originalValue
			+ "\" size=\"" + originalValue.length + "\">" + "</form>";
	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveButton(editArg, formid)
			+ buildCancelButton();
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}
}

function modifyNamespaceCell(caller, editArg) {
	modifyMetaDataCell(caller, editArg);
}

function onOMMTypeSelectClick(caller, block, select) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=ommtype!' + block + '', {
			method : 'get',
			cache : true,
			onSuccess : function(transport) {
				var entitytypestr = transport.responseText || "no response";
				var entitytypes = entitytypestr.split(",");

				for ( var i = 0; i < entitytypes.length; i++) {
					var option = document.createElement("option");
					option.setAttribute("value", entitytypes[i]);
					option.innerHTML = entitytypes[i];
					if (select == entitytypes[i]) {
						option.setAttribute("selected", "selected");
					}
					caller.appendChild(option);
				}
			}
		});
	}
}

function modifyTypeCell(caller, editArg) {
	var originalValueCell = caller.childNodes[1];
	var originalValue = originalValueCell.childNodes[0].innerHTML;

	var typeSelect = document.createElement("select");
	typeSelect.setAttribute("name", "value");
	typeSelect.setAttribute("onclick", "onOMMTypeSelectClick(this,'"
			+ editArg.split("!")[0] + "', '" + originalValue + "')");
	var opt = document.createElement("option");
	opt.setAttribute("selected", "selected");
	opt.innerHTML = originalValue;
	typeSelect.appendChild(opt);

	removeAllChildren(originalValueCell);
	originalValueCell.appendChild(typeSelect);

	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveFormButton(editArg, caller
			.getAttribute("hash"))
			+ buildCancelButton();

	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.getAttribute("hash"));
	caller.appendChild(form);
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}

}

function onLinkChange(event) {

	var savebtn = event.target.parentNode.parentNode.parentNode.childNodes[2].childNodes[0];
	var saveclick = savebtn.getAttribute("onclick");
	// return;
	if (!checkSchema(event.target.value)) {

		savebtn
				.setAttribute(
						"onclick",
						"if(!linkFieldCheck(this.parentNode.parentNode.childNodes[1].childNodes[0])) return;" +
						" else "+saveclick+" ");
		highlight = new Effect.Highlight(event.target, {
			duration : 3,

		});
		shake = new Effect.Shake(event.target, {
			duration : 0.5,
			distance : 1
		});
	} else {

		highlight.cancel();
		shake.cancel();
	}
}

function linkFieldCheck(caller) {
	if (!validateURL(caller.childNodes[0].value)) {
		highlight = new Effect.Highlight(caller.childNodes[0], {
			duration : 3,

		});
		shake = new Effect.Shake(caller.childNodes[0], {
			duration : 0.5,
			distance : 1
		});
		return false;
	} else {
		highlight.cancel();
		shake.cancel();
		return true;
	}
}

function modifyLinkCell(caller, editArg) {
	var originalValueCell = caller.childNodes[1];
	var originalValue = originalValueCell.childNodes[0].childNodes[0]
			.getAttribute("src");
	var formid = caller.getAttribute("hash");
	// alert(formid);
	originalValueCell.innerHTML = "<form id=\""
			+ formid
			+ "\">"
			+ "<input name=\"value\" type=\"text\" oninput=\"onLinkChange(event);\" value=\""
			+ originalValue + "\" size=\"" + 80 + "\">" + "</form>";
	var actionCell = caller.childNodes[caller.childNodes.length - 1];
	actionCell.innerHTML = buildSaveButton(editArg, formid)
			+ buildCancelButton();
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}
}

function onPayloadRelationClick(caller, select) {
	if (caller.childNodes.length <= 1) {
		if (caller.childNodes.length == 1)
			caller.removeChild(caller.lastChild);
		new Ajax.Request('?cmd=download&part=payloadrelation!', {
			method : 'get',
			cache : true,
			onSuccess : function(transport) {
				var multilangsstr = transport.responseText || "no response";
				var multilangs = multilangsstr.split(",");

				for ( var i = 0; i < multilangs.length; i++) {
					var option = document.createElement("option");
					option.setAttribute("value", multilangs[i]);
					option.innerHTML = multilangs[i];
					if (select == multilangs[i]) {
						option.setAttribute("selected", "selected");
					}
					caller.appendChild(option);
				}
			}
		});
	}
}

function modifyPayloadCell(caller, editArg) {
	// alert(caller.innerHTML);
	var type = caller.parentNode.parentNode.parentNode.childNodes[0].childNodes[0].innerHTML;
	type = type.substring(0, type.length - 1);
	var originalKey, originalValue;
	var block = editArg.split("!")[0];
	switch (type) {
	case "OMM Indentification Information":
		var keycell = caller.childNodes[0];
		var actioncell = caller.childNodes[2];
		originalKey = keycell.innerHTML;
		var valuecell = caller.childNodes[1];
		if (valuecell.childNodes[0].tagName == "A") {
			originalValue = valuecell.childNodes[0].innerHTML;
		} else
			originalValue = valuecell.innerHTML;
		var keyselect = document.createElement("select");
		keyselect.setAttribute("id", "PayloadIdInfoKeySelect");
		keyselect.setAttribute("onClick", "onPayloadIdInfoKeysClick(this, '"
				+ block + "','" + originalKey + "')");
		keyselect.setAttribute("name", "key");
		var opt = document.createElement("option");
		opt.setAttribute("value", originalKey);
		opt.setAttribute("selected", "selected");
		opt.innerHTML = originalKey;
		keyselect.appendChild(opt);
		removeAllChildren(keycell);
		keycell.appendChild(keyselect);

		var valueinput = document.createElement("input");
		valueinput.setAttribute("type", "text");
		valueinput.setAttribute("name", "value");
		valueinput.setAttribute("size", originalValue.length);
		valueinput.setAttribute("value", originalValue);

		removeAllChildren(valuecell);
		valuecell.appendChild(valueinput);

		actioncell.innerHTML = buildSaveFormButton(editArg, caller
				.getAttribute("hash"))
				+ buildCancelButton();

		break;
	case "OMM Structure Information":
		var hash = caller.getAttribute("hash");
		var datecell = caller.ownerDocument.getElementById("date" + hash);
		var relationcell = caller.childNodes[0].ownerDocument
				.getElementById("relation" + hash);
		var targetcell = caller.childNodes[0].ownerDocument
				.getElementById("target" + hash);
		var actionCell = caller.childNodes[0].ownerDocument
				.getElementById("actions" + hash);
		// alert(datecell.innerHTML);
		var originalStartDateValue = datecell.innerHTML.substring(6, 6 + 10);
		var originalStartTimeValue = datecell.innerHTML.substring(16 + 1);
		var originalEndDateValue, originalEndTimeValue;
		if (datecell.innerHTML.length > 23) {
			originalStartDateValue = datecell.innerHTML.substring(0, 0 + 10);
			originalStartTimeValue = datecell.innerHTML.substring(10 + 1,
					10 + 1 + 5);
			originalEndDateValue = datecell.innerHTML.substring(16 + 7,
					16 + 7 + 10);
			originalEndTimeValue = datecell.innerHTML.substring(
					16 + 7 + 10 + 1, 16 + 7 + 10 + 1 + 5);
		} else {
			originalEndDateValue = "0000-00-00";
			originalEndTimeValue = "00:00:00";
		}

		var dateCellName = "date";
		// + dateCellCount++;
		datecell.innerHTML = "<input name=\""
				+ "startdate"
				+ "\" type=\"text\" value=\""
				+ originalStartDateValue
				+ "\" size=\"10\" onclick=\"displayCalendar(this.parentNode.childNodes[0],'yyyy-mm-dd',this, true)\">"
				+ "<input name=\"starttime\" type=\"text\" value=\""
				+ originalStartTimeValue
				+ "\" size=\"8\">"
				+ "</br>"
				+ "to </br>"
				+ "<input name=\""
				+ "enddate"
				+ "\" type=\"text\" value=\""
				+ originalEndDateValue
				+ "\" size=\"10\" onclick=\"displayCalendar(this.parentNode.childNodes[5],'yyyy-mm-dd',this, true)\">"
				+ "<input name=\"endtime\" type=\"text\" value=\""
				+ originalEndTimeValue + "\" size=\"8\">";

		var originalRelation = relationcell.innerHTML;
		var relationSelect = document.createElement("select");
		relationSelect.setAttribute("id", "relationSelect" + hash);
		relationSelect.setAttribute("name", "relation");
		relationSelect.setAttribute("onclick", "onPayloadRelationClick(this,'"
				+ originalRelation + "')");
		var opt = document.createElement("option");
		opt.innerHTML = originalRelation;
		opt.setAttribute("selected", "selected");
		relationSelect.appendChild(opt);
		removeAllChildren(relationcell);
		relationcell.appendChild(relationSelect);

		var originalTarget = targetcell.childNodes[1].innerHTML;
		var targetInput = document.createElement("input");
		targetInput.setAttribute("name", "target");
		targetInput.setAttribute("type", "text");
		targetInput.setAttribute("value", originalTarget);
		targetInput.setAttribute("size", originalTarget.length);
		removeAllChildren(targetcell);
		targetcell.appendChild(targetInput);

		actionCell.innerHTML = buildSaveFormButton(editArg, caller
				.getAttribute("hash"))
				+ buildCancelButton();
		break;
	default:
		break;
	}
	var form = document.createElement('form');
	form.style.display = "none";
	form.setAttribute('id', caller.getAttribute("hash"));
	caller.appendChild(form);
	if (caller != null) {
		new Effect.Highlight(caller, {
			duration : 5
		});
		Effect.ScrollTo(caller, {
			duration : '0.5'
		});
	}
}

function getBlockFormat(block) {
	var format;
	var myajax = new Ajax.Request('?cmd=download&part=getblockformat!' + block
			+ '', {
		method : 'get',
		asynchronous : false,
		onSuccess : function(transport) {
			format = transport.responseText || "no response";
		}
	});
	return format;
}

function modifyMetaPayloadCell(caller, editArg) {
	modifyMetaPayloadUploadCell(caller, editArg);
}

function modifyMetaPayloadUploadCell(caller, editArg) {
	var format = getBlockFormat(editArg.split("!")[0]);

	var linkcell = caller.childNodes[1];
	var actioncell = caller.childNodes[2];
	var divupload = document.createElement("div");
	divupload.setAttribute("id", caller.getAttribute("hash"));
	var divqquploader = document.createElement("div");
	divqquploader.setAttribute("class", "qq-uploader");

	var divuploaddroparea = document.createElement("div");
	divuploaddroparea.setAttribute("class", "qq-upload-drop-area");
	divuploaddroparea.setAttribute("style", "display: none;");
	divuploaddroparea.innerHTML = "<span>Drop you file here</span>";

	var divqquploadbtn = document.createElement("div");
	divqquploadbtn.setAttribute("class", "qq-upload-button");
	divqquploadbtn
			.setAttribute("style",
					"position: relative; overflow-x: hidden; overflow-y: hidden; direction: ltr; ");
	divqquploadbtn.innerHTML = "Upload a file"
			+ "<input multiple=\"multiple\" type=\"file\" name=\"file\""
			+ " style=\"position: absolute; right: 0px; top: 0px; font-family: Arial; font-size: 118px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; cursor: pointer; opacity: 0; \">";

	var qquploadlist = document.createElement("ul");
	qquploadlist.setAttribute("class", "qq-upload-list");

	divqquploader.appendChild(divuploaddroparea);
	divqquploader.appendChild(divqquploadbtn);
	divqquploader.appendChild(qquploadlist);
	divupload.appendChild(divqquploader);

	removeAllChildren(linkcell);
	linkcell.innerHTML = "";
	linkcell.appendChild(divupload);

	var uploader = new qq.FileUploader({
		element : linkcell.childNodes[0],
		// document.getElementById(caller.getAttribute("hash")),
		action : '?cmd=edit&part=' + editArg + '!'
				+ caller.getAttribute("class"),
		onComplete : function() {
			// alert(caller.innerHTML);
			new Ajax.Updater(caller.getAttribute("id"),
					'?cmd=download&part=update_' + editArg + '!'
							+ caller.getAttribute("class"), {
						method : 'get',
						cache : false,
						onSuccess : function(transport) {
							var response = transport.responseText
									|| "no response text";
							// alert("Success! \n\n" + response);
							var index = response.indexOf("hash");
							var hash = response.substring(index + 6,
									index + 64 + 6);
							caller.parentNode.parentNode.setAttribute("hash",
									hash);
						}
					});

		}
	});
	var actionCell = caller.childNodes[2];
	// actionCell.innerHTML = buildSaveMetaPayloadButton(editArg)
	// + buildCancelButton();
	actionCell.innerHTML = buildCancelButton();

}

function onMetaPayloadSaveClick(caller, editArg, metaType) {
	var tabletuple = caller.parentNode.parentNode;
	var oldid = tabletuple.getAttribute("id");
	tabletuple.setAttribute("id", oldid + tabletuple.getAttribute("hash"));
	// alert(tabletuple.getAttribute("id"));
	new Ajax.Updater(
			tabletuple.getAttribute("id"),
			'?cmd=download&part=update_' + editArg + '!'
					+ caller.parentNode.parentNode.getAttribute("class") + '&',
			{
				method : 'get',
				cache : false,
				asynchronous : false,
				onSuccess : function(transport) {
					var response = transport.responseText || "no response text";
					// alert("Success! \n\n" + response);
					var index = response.indexOf("hash");
					var hash = response.substring(index + 6, index + 64 + 6);
					caller.parentNode.parentNode.setAttribute("hash", hash);
				}
			});
	tabletuple.setAttribute("id", oldid);
}

function editEntry(caller, entryMeta, editArg) {
	originalCallerHTML = caller.innerHTML;
	addCache(caller.getAttribute("hash"), caller.innerHTML);
	editArg = editArg + "!hash_" + caller.getAttribute("hash");
	if (entryMeta == "Title" && caller.tagName == "TR") {
		modifyValueActionCell(caller, editArg);

	} else if (entryMeta == "Creator" && caller.tagName == "TR") {
		modifyValueDateActionCell(caller, editArg);
	} else if (entryMeta == "Contributors" && caller.tagName == "TR") {
		modifyValueDateActionCell(caller, editArg);
	} else if (entryMeta == "Description" && caller.tagName == "TR") {
		modifyValueActionCell(caller, editArg);
	} else if (entryMeta == "Format" && caller.tagName == "TR") {
		addCache(caller.getAttribute("hash"), caller.parentNode.innerHTML);
		modifyFormatCell(caller, editArg);
	} else if (entryMeta == "Subject" && caller.tagName == "TR") {
		modifySubjectCell(caller, editArg);
	} else if (entryMeta == "Namespace" && caller.tagName == "TR") {
		modifyNamespaceCell(caller, editArg);
	} else if (entryMeta == "Type" && caller.tagName == "TR") {
		modifyTypeCell(caller, editArg);
	} else if (entryMeta == "Payload" && caller.tagName == "TR") {
		var editArgArray = editArg.split("!");
		if (editArgArray.length == 4) {
			modifyPayloadCell(caller, editArg);
		} else {
			modifyMetaPayloadCell(caller, editArg);
		}
	} else if (entryMeta == "Link" && caller.tagName == "TR") {
		modifyLinkCell(caller, editArg);
	} else {
		originalCallerHTML = null;
	}
}

var originalCallerHTML = null;

var originalHTMLCache = new Array();
function addCache(key, value) {
	originalHTMLCache[key] = value;
}

function getCache(key) {
	var ret = originalHTMLCache[key];
	delete originalHTMLCache[key];
	return ret;
}

function onCancelAdditionClick(caller) {
	var elem = caller.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;

	Effect.Fade(elem, {
		duration : 0.5,
		afterFinish : function() {
			elem.parentNode.removeChild(elem);
		}
	});
}

function onCancelAddEntryClick(caller) {
	var elem = caller;

	Effect.Fade(elem, {
		duration : 0.5,
		afterFinish : function() {
			elem.parentNode.removeChild(elem);
		}
	});
}

function onCancelClick(caller) {
	var formatIdAttr = caller.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode
			.getAttribute("id");
	if (formatIdAttr != null && formatIdAttr.split("_")[0] == "Format") {
		caller.parentNode.parentNode.parentNode.innerHTML = getCache(caller.parentNode.parentNode
				.getAttribute("hash"));
		originalCallerHTML = null;

	} else {
		caller.parentNode.parentNode.innerHTML = getCache(caller.parentNode.parentNode
				.getAttribute("hash"));
		originalCallerHTML = null;
	}
}

function onSaveClick(caller, editArg, metaType, formid) {
	var serialform = $(formid).serialize();
	// alert(caller.parentNode.parentNode.innerHTML);
	new Ajax.Updater(
			caller.parentNode.parentNode.getAttribute("id"),
			'?cmd=edit&part=' + editArg + '!'
					+ caller.parentNode.parentNode.getAttribute("class") + '&'
					+ serialform,
			{
				method : 'post',
				cache : false,
				onSuccess : function(transport) {
					var response = transport.responseText || "no response text";
					// alert("Success! \n\n" + response);
					var index = response.indexOf("hash");
					var hash = response.substring(index + 6, index + 64 + 6);
					caller.parentNode.parentNode.setAttribute("hash", hash);
				}
			});
}

function getModifiedValue(caller, metaType) {
	var ret;
	switch (metaType) {
	case "Title":

		break;
	default:
		break;
	}
}

function buildCancelAdditionButton() {
	return buildButton("Cancel", "Cancel", "onCancelAdditionClick(this)");
}

function buildCancelAddEntryButton(entryRefStr) {
	return buildButton("Cancel", "Cancel", "onCancelAddEntryClick("
			+ entryRefStr + ")");
}

function buildCancelButton() {
	return buildButton("Cancel", "Cancel", "onCancelClick(this)");
}

function buildSaveButton(editArg, formid) {
	// alert("edit" + editArg);
	var metaType = editArg.split("!")[1].split("_")[0];
	// alert("metaType" + metaType);
	return buildButton("Save", "Save change", "onSaveClick(this, '" + editArg
			+ "', '" + metaType + "', '" + formid + "')");
}

function buildSaveAdditionButton() {
	return buildButton("Save", "Save change", "onSaveClick(this)");
}

function buildButton(innerText, tooltipText, onclick) {

	var buttonHTML = "<span class=\"button\" value=\"click\" title=\""
			+ tooltipText + "\"" + " onclick=\"new " + onclick + ";\">"
			+ "<span style=\"font-size:1.5em;\"></span>" + "&nbsp;" + innerText
			+ "&nbsp;&nbsp;" + "</span>";
	return buttonHTML;
}

function getAddTerm(addCmd) {
	return addCmd.split("&")[1];
}

function checkTagAndId(callerTagName, callerId) {
	if (callerTagName == "TR" && callerId.split("_")[0] == "Title") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Namespace") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Creator") {
		return true;
	} else if (callerTagName == "TR"
			&& callerId.split("_")[0] == "Contributors") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Type") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Format") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Description") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Payload") {
		return true;
	} else if (callerTagName == "ARTICLE" && callerId.split("_")[0] == "block") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "entry") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Subject") {
		return true;
	} else if (callerTagName == "TR" && callerId.split("_")[0] == "Link") {
		return true;
	} else if (callerTagName == "HEADER" && callerId == "header") {
		return true;
	}

	return false;
}