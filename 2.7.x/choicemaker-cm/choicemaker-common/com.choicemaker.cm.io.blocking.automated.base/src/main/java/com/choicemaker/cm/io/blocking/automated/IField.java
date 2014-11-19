package com.choicemaker.cm.io.blocking.automated;

import java.io.Serializable;

public interface IField extends Serializable {

	IField[][] getIllegalCombinations();

}