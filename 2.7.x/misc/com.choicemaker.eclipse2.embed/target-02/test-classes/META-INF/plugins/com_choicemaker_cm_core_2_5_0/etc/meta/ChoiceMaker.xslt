<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes">
    <xsl:output version="2.0" encoding="UTF-8" indent="no" omit-xml-declaration="no" media-type="text/html" />
    
    
 <xsl:template match="/">
        <html>
            <head>
                <title />
            </head>
            <h2>NSW Record Schema</h2>
            <body>
            <font face="Arial" size="8">
                <xsl:for-each select="ChoiceMakerSchema">
                   <xsl:for-each select="nodeType">
                        <table border="1" cellspacing="0" cellpadding="2" width="100%" > 
                            <tbody>
                                <tr>
										<td width="12%" align="center"><b>Node</b></td>
										<td width="10%" align="center"><b>Field</b></td>
										<td width="5%" align="center"><b>Type</b></td>
										<td width="30%" align="center"><b>Validation</b></td>
										<td width="30%" align="center"><b>Description</b></td>                                          
                                </tr>
<!--                      																								-->                                
                                    <xsl:for-each select="field">
											<xsl:if test="not (./derived)">                                
													<tr >
														<td >
															<xsl:for-each select="../@name">
																<xsl:value-of select="." />
															</xsl:for-each>
														</td>
													   <td  >
															<xsl:for-each select="@name">
																<xsl:value-of select="." />
															</xsl:for-each>
														</td>
														<td >
															<xsl:for-each select="@type">
																<xsl:value-of select="." />
															</xsl:for-each>
														</td>
														<td >
															<xsl:for-each select="@valid">
																<xsl:call-template  name ="formatValidation" >
																	<xsl:with-param name="vstr" select="." />
																</xsl:call-template>
															</xsl:for-each>
														</td>
														<td>
															<xsl:choose>
																<xsl:when test="comment">
																		<xsl:value-of select="comment" />
																</xsl:when>
																<xsl:otherwise>
																	<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
																</xsl:otherwise>
															</xsl:choose>
														</td>
													</tr>		
										</xsl:if>        
									</xsl:for-each>                                     
								    <xsl:for-each select="nodeType">
<!--                      																								-->                                								    								    
											<xsl:for-each select="field">
													<xsl:if test="not (./derived)">                                
															<tr >
																<td >
																	 <xsl:for-each select="../../@name">
																		<xsl:value-of select="." />
																	</xsl:for-each>/<xsl:for-each select="../@name">
																		<xsl:value-of select="." />
																	</xsl:for-each>
																</td>
															   <td>
																	<xsl:for-each select="@name">
																		<xsl:value-of select="." />
																	</xsl:for-each>
																</td>
																<td >
																	<xsl:for-each select="@type">
																		<xsl:value-of select="." />
																	</xsl:for-each>
																</td>
																<td>
																		<xsl:for-each select="@valid">
																			<xsl:call-template  name ="formatValidation" >
																				<xsl:with-param name="vstr" select="." />
																			</xsl:call-template>
																		</xsl:for-each>
																</td>
																<td>
																	<xsl:choose>
																		<xsl:when test="comment">
																				<xsl:value-of select="comment" />
																		</xsl:when>
																		<xsl:otherwise>
																			<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
																		</xsl:otherwise>
																	</xsl:choose>
																</td>
															</tr>		
												</xsl:if>        
											</xsl:for-each>                                     
<!--                      																								-->                                								    
											<xsl:for-each select="nodeType">
<!--                      																								-->                                								    								    
													<xsl:for-each select="field">
															<xsl:if test="not (./derived)">                                
																	<tr >
																		<td>
																			 <xsl:for-each select="../../../@name">
																				<xsl:value-of select="." />
																			</xsl:for-each>/																				
																			 <xsl:for-each select="../../@name">
																				<xsl:value-of select="." />
																			</xsl:for-each>/
																			<xsl:for-each select="../@name">
																				<xsl:value-of select="." />
																			</xsl:for-each>
																		</td>
																	   <td  >
																			<xsl:for-each select="@name">
																				<xsl:value-of select="." />
																			</xsl:for-each>
																		</td>
																		<td>
																			<xsl:for-each select="@type">
																				<xsl:value-of select="." />
																			</xsl:for-each>
																		</td>
																		<td>
																				<xsl:for-each select="@valid">
																					<xsl:call-template  name ="formatValidation" >
																						<xsl:with-param name="vstr" select="." />
																					</xsl:call-template>
																				</xsl:for-each>
																		</td>
																		<td>
																			<xsl:choose>
																				<xsl:when test="comment">
																						<xsl:value-of select="comment" />
																				</xsl:when>
																				<xsl:otherwise>
																					<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
																				</xsl:otherwise>
																			</xsl:choose>
																		</td>
																	</tr>		
														</xsl:if>        
													</xsl:for-each>                                     
<!--                      																								-->                                								    
                                <xsl:for-each select="nodeType">
                                            <tr>
                                                <td width="373">/<xsl:for-each select="@name">
                                                        <xsl:value-of select="." />
                                                    </xsl:for-each>/<xsl:for-each select="@name">
                                                        <xsl:value-of select="." />
                                                    </xsl:for-each>/<xsl:for-each select="@name">
                                                        <xsl:value-of select="." />
                                                    </xsl:for-each>/<xsl:for-each select="@name">
                                                        <xsl:value-of select="." />
                                                    </xsl:for-each>
                                                </td>
                                                <td colspan="4" width="1000">
                                                   <table border="1" width="100%" cellspacing="1" >                                                
                                                     <xsl:for-each select="field">
                                                            <tbody>
                                                             <xsl:if test="not (./derived)">
                                                    <tr>
                                                        <td width="181">
                                                            <xsl:for-each select="@name">
                                                                <xsl:value-of select="." />
                                                            </xsl:for-each>
                                                        </td>
                                                        <td width="147">
                                                            <xsl:for-each select="@type">
                                                                <xsl:value-of select="." />
                                                            </xsl:for-each>
                                                        </td>
                                                        <td width="332">
                                                            <xsl:for-each select="content">
                                                                <xsl:value-of select="." />
                                                            </xsl:for-each>
                                                        </td>
                                                        <td>
                                                            <xsl:for-each select="comment">
                                                                <xsl:value-of select="." />
                                                            </xsl:for-each>
                                                        </td>
                                                    </tr>
                                                               </xsl:if> 
                                                            </tbody>
														</xsl:for-each>                                                            
                                                     </table>
                                                </td>
                                            </tr>
                                </xsl:for-each>
                            </xsl:for-each>
                        </xsl:for-each>
                                        </tbody>
                                    </table>
                        
                    </xsl:for-each>
                </xsl:for-each>
                <br />
              </font>  
            </body>
        </html>
    </xsl:template>
    
	<xsl:template name="formatValidation">
		<xsl:param name="vstr"/>
		<xsl:choose>
		
			<xsl:when test="contains($vstr,'Sets.includes(')">
				<xsl:variable name="prefix" select="substring-before($vstr,'Sets.includes(')"></xsl:variable>
				<xsl:variable name="temp" select="substring-after($vstr,'Sets.includes(')"></xsl:variable>
				<xsl:variable name="fconst" select="substring-before($temp,',')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($temp,')')"></xsl:variable>

				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="concat($prefix,'BELONG TO ',$fconst,$suffix)" />
				</xsl:call-template>
			</xsl:when>

		
			<xsl:when test="contains($vstr,'StringUtils.nonEmptyString')">
				<xsl:variable name="prefix" select="substring-before($vstr,'StringUtils.nonEmptyString')"></xsl:variable>
				<xsl:variable name="temp" select="substring-after($vstr,'StringUtils.nonEmptyString(')"></xsl:variable>
				<xsl:variable name="fconst" select="substring-before($temp,')')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($temp,')')"></xsl:variable>

				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="concat($prefix,'NOT EMPTY',$suffix)" />
				</xsl:call-template>
			</xsl:when>
		
			<xsl:when test="contains($vstr,'StringUtils')">
				<xsl:variable name="prefix" select="substring-before($vstr,'StringUtils')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($vstr,'StringUtils.')"></xsl:variable>
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="concat($prefix,$suffix)" />
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="contains($vstr,'DateUtils.get')">
				<xsl:variable name="prefix" select="substring-before($vstr,'DateUtils.get')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($vstr,'DateUtils.get')"></xsl:variable>
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="concat($prefix,$suffix)" />
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="contains($vstr,'&amp;&amp;')">
				<xsl:variable name="prefix" select="substring-before($vstr,'&amp;&amp;')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($vstr,'&amp;&amp;')"></xsl:variable>
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$prefix" />
				</xsl:call-template>
				AND<br />
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$suffix" />
				</xsl:call-template>
			</xsl:when>

			<xsl:when test="contains($vstr,'linebreak;')">
				<xsl:variable name="prefix" select="substring-before($vstr,'linebreak;')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($vstr,'linebreak;')"></xsl:variable>
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$prefix" />
				</xsl:call-template>
				<br />
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$suffix" />
				</xsl:call-template>
			</xsl:when>


			<xsl:when test="contains($vstr,'.equals(')">
				<xsl:variable name="prefix" select="substring-before($vstr,'.equals(')"></xsl:variable>
				<xsl:variable name="temp" select="substring-after($vstr,'.equals(')"></xsl:variable>
				<xsl:variable name="fconst" select="substring-before($temp,')')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($temp,')')"></xsl:variable>
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$prefix" />
				</xsl:call-template>
						   = 
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$fconst" />
				</xsl:call-template>
					<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$suffix" />
				</xsl:call-template>
			</xsl:when>



			<xsl:when test="contains($vstr,'!')">
				<xsl:variable name="prefix" select="substring-before($vstr,'!;')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after($vstr,'!')"></xsl:variable>
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$prefix" />
				</xsl:call-template>
			   NOT
				<xsl:call-template  name ="formatValidation" >
							<xsl:with-param name="vstr" select="$suffix" />
				</xsl:call-template>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$vstr"/>
			</xsl:otherwise>	
		</xsl:choose>	
	</xsl:template>
    

	<xsl:template match="@valid">
		<xsl:param name="vstr"/>
		<xsl:choose>
			<xsl:when test="contains(.,'StringUtils')">
				<xsl:variable name="prefix" select="substring-before(.,'StringUtils.')"></xsl:variable>
				<xsl:variable name="suffix" select="substring-after(.,'StringUtils.')"></xsl:variable>
				<xsl:value-of select="$prefix"/><xsl:value-of select="$suffix"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="."/>
			</xsl:otherwise>	
		</xsl:choose>	
	</xsl:template>

    
    
</xsl:stylesheet>
