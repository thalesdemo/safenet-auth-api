/**
 * Copyright 2023 safenet-auth-api
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  
 *
 * Represents the request body for the grid rendering endpoint.
 * The request body contains a grid challenge data string, which is used to generate a graphical challenge for the user to solve.
 *
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 * @schema(description = "The request body for the render grid endpoint")
 */
 
package com.thalesdemo.safenet.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "The request body for the render grid endpoint")
public class GridRenderRequest {
	@Schema(description = "Grid challenge data string", example="1111122222333334444455555")
	private String string;

	/**
	 * @return the string
	 */
	public String getString() {
		return string;
	}

	/**
	 * @param string the string to set
	 */
	public void setString(String string) {
		this.string = string;
	}
	
	
}