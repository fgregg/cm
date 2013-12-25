var quickSearchDefaultValue = 'Search';

function resetInput (theInput) {
	if (theInput.value == quickSearchDefaultValue) theInput.value = '';
}

function getObj (objID) {
	return document.getElementById (objID);
}