// Form Validation Functions  v1.1.4
// http://www.dithered.com/javascript/form_validation/index.html
// code by Chris Nott (chris@NOSPAMdithered.com - remove NOSPAM)


// Revised Nov. 27, 2002 by Joe Ashear (revision to isValidEmail() function; see comment there)
// Revised Dec. 15, 2003 by Joe Ashear (added minvalue and maxvalue features)
// Revised Jun. 16, 2004 by Joe Ashear (made it so that the pattern functions are not mutually exclusive of the other filters)
// Revised Jun. 17, 2004 by Joe Ashear (made it so that the pattern tests are only run if the field has a value)

function getFormErrors(form) {
   var errors = new Array();
   // loop thru all form elements
   for (var elementIndex = 0; elementIndex < form.elements.length; elementIndex++) {
      var element = form.elements[elementIndex];
     
     var hasPattern = element.pattern;
     // the field has a value if its value is non-empty OR if it has a phone prefix and a phone suffix and a value, all of which are non-empty
     var hasValue = ((element.value.length) || (element.prefix && element.prefix.length && element.suffix && element.suffix.length && element.value.length));
          
	 // pattern (credit card number, email address, zip or postal code, alphanumeric, numeric)
	  if (hasPattern && hasValue) {
		if ( ( (element.pattern.toLowerCase() == 'visa' || element.pattern.toLowerCase() == 'mastercard' || element.pattern.toLowerCase() == 'american express' || element.pattern.toLowerCase() == 'diners club' || element.pattern.toLowerCase() == 'discover' || element.pattern.toLowerCase() == 'enroute' || element.pattern.toLowerCase() == 'jcb' || element.pattern.toLowerCase() == 'credit card') && isValidCreditCard(element.value, element.pattern) == false) ||
			   (element.pattern.toLowerCase() == 'email' && isValidEmailStrict(element.value) == false) ||
			   (element.pattern.toLowerCase() == 'zip or postal code' && isValidZipcode(element.value) == false && isValidPostalcode(element.value) == false) ||
			   (element.pattern.toLowerCase() == 'zipcode' && isValidZipcode(element.value) == false) ||
			   (element.pattern.toLowerCase() == 'postal code' && isValidPostalcode(element.value) == false) ||
			   (element.pattern.toLowerCase() == 'phone' &&  ( (element.prefix && element.suffix && isValidUSPhoneNumber(element.value, form[element.prefix].value, form[element.suffix].value) == false) || (isValidUSPhoneNumber(element.value) == false) ) ) ||
			   (element.pattern.toLowerCase() == 'alphanumeric' && isAlphanumeric(element.value, true) == false) ||
			   (element.pattern.toLowerCase() == 'numeric' && isNumeric(element.value, true) == false) ||
			   (element.pattern.toLowerCase() == 'alphabetic' && isAlphabetic(element.value, true) == false) ) {
		   errors[errors.length] = element.patternError;
		}
	 }
	 
     
      // text and textarea types
      if (element.type == "text" || element.type == "textarea") {
         element.value = trimWhitespace(element.value)
         
         // required element
         if (element.required  && element.value == '') {
            errors[errors.length] = element.requiredError;
         }
         
         // maximum length
         else if (element.maxlength && isValidLength(element.value, 0, element.maxlength) == false) {
            errors[errors.length] = element.maxlengthError;
         }

         // minimum length
         else if (element.minlength && isValidLength(element.value, element.minlength, Number.MAX_VALUE) == false) {
            errors[errors.length] = element.minlengthError;
         }
         
         // maximum value
         else if (element.maxvalue && isValidMaximumValue(element.value, element.maxvalue) == false) {
            errors[errors.length] = element.maxvalueerror;
         }
         
         // minimum value
         else if (element.minvalue && isValidMinimumValue(element.value, element.minvalue) == false) {
            errors[errors.length] = element.minvalueerror;
         }
         
      }
      
      // password 
      else if (element.type == "password") {
         
         // required element
         if (element.required  && element.value == '') {
            errors[errors.length] = element.requiredError;
         }
         
         // maximum length
         else if (element.maxlength && isValidLength(element.value, 0, element.maxlength) == false) {
            errors[errors.length] = element.maxLengthError;
         }

         // minimum length
         else if (element.minlength && isValidLength(element.value, element.minlength, Number.MAX_VALUE) == false) {
            errors[errors.length] = element.minLengthError;
         }
      }
      
      // file upload
      if (element.type == "file") {
         
         // required element
         if (element.required  && element.value == '') {
            errors[errors.length] = element.requiredError;
         }
      }
      
      // select
      else if (element.type == "select-one" || element.type == "select-multiple" || element.type == "select") {

         // required element
         if (element.required && element.selectedIndex == -1) {
            errors[errors.length] = element.requiredError;
         }
		 
		 // required element
         if (element.required && element.value == "noanswer") {
            errors[errors.length] = element.requiredError;
         }
         
		 // disallow empty value selection
         else if (element.disallowEmptyValue && element.options[element.selectedIndex].value == '') {
            errors[errors.length] = element.disallowEmptyValueError;
         }

      }
      
      // radio buttons
      else if (element.type == "radio") {
         
         // required element
         if (element.required && element.length) {
            var checkedRadioButton = -1;
            for (var radioIndex = 0; radioIndex < element.length; radioIndex++) {
               if (element[radioIndex].checked == true) {
                  checkedRadioButton = radioIndex;
                  break;
               }
            }
            if (checkedRadioButton == -1) {
               errors[errors.length] = element.requiredError;
            }
         }
      }
   }
   
	return errors;
}


// Check that the number of characters in a string is between a max and a min
function isValidLength(string, min, max) {
	if (string.length < min || string.length > max) return false;
	else return true;
}


// Check that the value of a string is greater than or equal to a given minimum
function isValidMinimumValue(string, minimumValue) {
	if (string < minimumValue) return false;
	else return true;
}

// Check that the value of a string is less than or equal to than a given maximum
function isValidMaximumValue(string, maximumValue) {
	if (string > maximumValue) return false;
	else return true;
}


// Check that a credit card number is valid based using the LUHN formula (mod10 is 0)
function isValidCreditCard(number) {
	number = '' + number;
	
	if (number.length > 16 || number.length < 13 ) return false;
	else if (getMod10(number) != 0) return false;
	else if (arguments[1]) {
		var type = arguments[1];
		var first2digits = number.substring(0, 2);
		var first4digits = number.substring(0, 4);
		
		if (type.toLowerCase() == 'visa' && number.substring(0, 1) == 4 &&
			(number.length == 16 || number.length == 13 )) return true;
		else if (type.toLowerCase() == 'mastercard' && number.length == 16 &&
			(first2digits == '51' || first2digits == '52' || first2digits == '53' || first2digits == '54' || first2digits == '55')) return true;
		else if (type.toLowerCase() == 'american express' && number.length == 15 && 			(first2digits == '34' || first2digits == '37')) return true;
		else if (type.toLowerCase() == 'diners club' && number.length == 14 && 			(first2digits == '30' || first2digits == '36' || first2digits == '38')) return true;
		else if (type.toLowerCase() == 'discover' && number.length == 16 && first4digits == '6011') return true;
		else if (type.toLowerCase() == 'enroute' && number.length == 15 && 			(first4digits == '2014' || first4digits == '2149')) return true;
		else if (type.toLowerCase() == 'jcb' && number.length == 16 &&
			(first4digits == '3088' || first4digits == '3096' || first4digits == '3112' || first4digits == '3158' || first4digits == '3337' || first4digits == '3528')) return true;
		
    // if the above card types are all the ones that the site accepts, change the line below to 'else return false'
    else return true;
	}
	else return true;
}

// Check that an email address is valid based on RFC 821 (?)
function isValidEmail(address) {
	// this "1" was changed from "3" by Joe Ashear Nov 27, 2002
	if (address.indexOf('@') < 1) return false;
	var name = address.substring(0, address.indexOf('@'));
	var domain = address.substring(address.indexOf('@') + 1);
	if (name.indexOf('(') != -1 || name.indexOf(')') != -1 || name.indexOf('<') != -1 || name.indexOf('>') != -1 || name.indexOf(',') != -1 || name.indexOf(';') != -1 || name.indexOf(':') != -1 || name.indexOf('\\') != -1 || name.indexOf('"') != -1 || name.indexOf('[') != -1 || name.indexOf(']') != -1 || name.indexOf(' ') != -1) return false;
	if (domain.indexOf('(') != -1 || domain.indexOf(')') != -1 || domain.indexOf('<') != -1 || domain.indexOf('>') != -1 || domain.indexOf(',') != -1 || domain.indexOf(';') != -1 || domain.indexOf(':') != -1 || domain.indexOf('\\') != -1 || domain.indexOf('"') != -1 || domain.indexOf('[') != -1 || domain.indexOf(']') != -1 || domain.indexOf(' ') != -1) return false;
	return true;
}


// Check that an email address has the form something@something.something
// This is a stricter standard than RFC 821 (?) which allows addresses like postmaster@localhost
function isValidEmailStrict(address) {
	if (isValidEmail(address) == false) return false;
	var domain = address.substring(address.indexOf('@') + 1);
	if (domain.indexOf('.') == -1) return false;
	if (domain.indexOf('.') == 0 || domain.indexOf('.') == domain.length - 1) return false;
	return true;
}


// Check that a US zip code is valid
function isValidZipcode(zipcode) {
	zipcode = removeSpaces(zipcode);
	if (!(zipcode.length == 5 || zipcode.length == 9 || zipcode.length == 10)) return false;
   if ((zipcode.length == 5 || zipcode.length == 9) && !isNumeric(zipcode)) return false;
   if (zipcode.length == 10 && zipcode.search && zipcode.search(/^\d{5}-\d{4}$/) == -1) return false;
   return true;
}


// Check that a Canadian postal code is valid
function isValidPostalcode(postalcode) {
	if (postalcode.search) {
		postalcode = removeSpaces(postalcode);
		if (postalcode.length == 6 && postalcode.search(/^[a-zA-Z]\d[a-zA-Z]\d[a-zA-Z]\d$/) != -1) return true;
		else if (postalcode.length == 7 && postalcode.search(/^[a-zA-Z]\d[a-zA-Z]-\d[a-zA-Z]\d$/) != -1) return true;
		else return false;
	}
	return true;
}

// Check that a US or Canadian phone number is valid
function isValidUSPhoneNumber(areaCode, prefixNumber, suffixNumber) {
   if (arguments.length == 1) {
      var phoneNumber = arguments[0];
      phoneNumber = phoneNumber.replace(/\D+/g, '');
      var length = phoneNumber.length;
      if (phoneNumber.length >= 7) {
         var areaCode = phoneNumber.substring(0, length-7);
         var prefixNumber = phoneNumber.substring(length-7, length-4);
         var suffixNumber = phoneNumber.substring(length-4);
      }
      else return false;
   }
   else if (arguments.length == 3) {
      var areaCode = arguments[0];
      var prefixNumber = arguments[1];
      var suffixNumber = arguments[2];
   }
   else return true;
   //alert(areaCode+":"+prefixNumber+":"+suffixNumber);
   if (areaCode.length != 3 || !isNumeric(areaCode) || prefixNumber.length != 3 || !isNumeric(prefixNumber) || suffixNumber.length != 4 || !isNumeric(suffixNumber)) return false;
   return true;
}

// Check that a string contains only letters and numbers
function isAlphanumeric(string, ignoreWhiteSpace) {
	if (string.search) {
		if ((ignoreWhiteSpace && string.search(/[^\w\s]/) != -1) || (!ignoreWhiteSpace && string.search(/\W/) != -1)) return false;
	}
	return true;
}

// Check that a string contains only letters
function isAlphabetic(string, ignoreWhiteSpace) {
	if (string.search) {
		if ((ignoreWhiteSpace && string.search(/[^a-zA-Z\s]/) != -1) || (!ignoreWhiteSpace && string.search(/[^a-zA-Z]/) != -1)) return false;
	}
	return true;
}

// Check that a string contains only numbers
function isNumeric(string, ignoreWhiteSpace) {
	if (string.search) {
		if ((ignoreWhiteSpace && string.search(/[^\d\s]/) != -1) || (!ignoreWhiteSpace && string.search(/\D/) != -1)) return false;
	}
	return true;
}

// Remove characters that might cause security problems from a string 
function removeBadCharacters(string) {
	if (string.replace) {
		string.replace(/[<>\"\'%;\)\(&\+]/, '');
	}
	return string;
}

// Remove all spaces from a string
function removeSpaces(string) {
	var newString = '';
	for (var i = 0; i < string.length; i++) {
		if (string.charAt(i) != ' ') newString += string.charAt(i);
	}
	return newString;
}

// Remove leading and trailing whitespace from a string
function trimWhitespace(string) {
	var newString  = '';
	var substring  = '';
	beginningFound = false;
	
	// copy characters over to a new string
	// retain whitespace characters if they are between other characters
	for (var i = 0; i < string.length; i++) {
		
		// copy non-whitespace characters
		if (string.charAt(i) != ' ' && string.charCodeAt(i) != 9) {
			
			// if the temporary string contains some whitespace characters, copy them first
			if (substring != '') {
				newString += substring;
				substring = '';
			}
			newString += string.charAt(i);
			if (beginningFound == false) beginningFound = true;
		}
		
		// hold whitespace characters in a temporary string if they follow a non-whitespace character
		else if (beginningFound == true) substring += string.charAt(i);
	}
	return newString;
}

// Returns a checksum digit for a number using mod 10
function getMod10(number) {
	
	// convert number to a string and check that it contains only digits
	// return -1 for illegal input
	number = '' + number;
	number = removeSpaces(number);
	if (!isNumeric(number)) return -1;
	
	// calculate checksum using mod10
	var checksum = 0;
	for (var i = number.length - 1; i >= 0; i--) {
		var isOdd = ((number.length - i) % 2 != 0) ? true : false;
		digit = number.charAt(i);
		
		if (isOdd) checksum += parseInt(digit);
		else {
			var evenDigit = parseInt(digit) * 2;
			if (evenDigit >= 10) checksum += 1 + (evenDigit - 10);
			else checksum += evenDigit;
		}
	}
	return (checksum % 10);
}

function showErrors (errors) {
	if (errors.length > 0) {
               var errorMessage = 'The form was not submitted due to the following problem' + ((errors.length > 1) ? 's' : '') + ':\n\n';
               for (var errorIndex = 0; errorIndex < errors.length; errorIndex++) {
                  errorMessage += '* ' + errors[errorIndex] + '\n';
               }
               errorMessage += '\nPlease fix ' + ((errors.length > 1) ? 'these' : 'this') + ' problem' + ((errors.length > 1) ? 's' : '') + ' and resubmit the form.';
               alert(errorMessage);
               return false;
            }
            
		// no errors: return true
		return true;
}