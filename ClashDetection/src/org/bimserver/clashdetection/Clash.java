package org.bimserver.clashdetection;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import org.bimserver.models.ifc2x3tc1.IfcProduct;

public class Clash {

	private IfcProduct ifcProduct1;
	private IfcProduct ifcProduct2;

	public Clash(IfcProduct ifcProduct1, IfcProduct ifcProduct2) {
		this.ifcProduct1 = ifcProduct1;
		this.ifcProduct2 = ifcProduct2;
	}
	
	public IfcProduct getIfcProduct1() {
		return ifcProduct1;
	}
	
	public IfcProduct getIfcProduct2() {
		return ifcProduct2;
	}
}
