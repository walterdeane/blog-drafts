<?xml version="1.0" encoding="UTF-8"?><sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns="http://www.opengis.net/sld" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>Default Styler</sld:Name>
    <sld:UserStyle>
      <sld:Name>Default Styler</sld:Name>
      <sld:Title>Populated places, scale-dependent</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Name>name</sld:Name>
        <sld:Rule>
          <sld:Title>Major cities</sld:Title>
          <ogc:Filter>
            <ogc:Or>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>is_capital</ogc:PropertyName>
                <ogc:Literal>true</ogc:Literal>
              </ogc:PropertyIsEqualTo>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>pop_max</ogc:PropertyName>
                <ogc:Literal>1000000</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
            </ogc:Or>
          </ogc:Filter>
          <sld:MaxScaleDenominator>2.5E7</sld:MaxScaleDenominator>
          <sld:PointSymbolizer>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>circle</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">#8C4A2F</sld:CssParameter>
                </sld:Fill>
                <sld:Stroke>
                  <sld:CssParameter name="stroke">#5C2E1A</sld:CssParameter>
                </sld:Stroke>
              </sld:Mark>
              <sld:Size>9</sld:Size>
            </sld:Graphic>
          </sld:PointSymbolizer>
          <sld:TextSymbolizer>
            <sld:Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">Noto Sans</sld:CssParameter>
              <sld:CssParameter name="font-size">12</sld:CssParameter>
              <sld:CssParameter name="font-style">normal</sld:CssParameter>
              <sld:CssParameter name="font-weight">bold</sld:CssParameter>
            </sld:Font>
            <sld:LabelPlacement>
              <sld:PointPlacement>
                <sld:AnchorPoint>
                  <sld:AnchorPointX>0.5</sld:AnchorPointX>
                  <sld:AnchorPointY>0.0</sld:AnchorPointY>
                </sld:AnchorPoint>
                <sld:Displacement>
                  <sld:DisplacementX>0</sld:DisplacementX>
                  <sld:DisplacementY>6</sld:DisplacementY>
                </sld:Displacement>
              </sld:PointPlacement>
            </sld:LabelPlacement>
            <sld:Halo>
              <sld:Radius>1.5</sld:Radius>
              <sld:Fill>
                <sld:CssParameter name="fill">#F5EDE0</sld:CssParameter>
              </sld:Fill>
            </sld:Halo>
            <sld:Fill>
              <sld:CssParameter name="fill">#5C2E1A</sld:CssParameter>
            </sld:Fill>
            <sld:VendorOption name="autoWrap">60</sld:VendorOption>
          </sld:TextSymbolizer>
        </sld:Rule>
        <sld:Rule>
          <sld:Title>Towns</sld:Title>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsLessThan>
                <ogc:PropertyName>pop_max</ogc:PropertyName>
                <ogc:Literal>1000000</ogc:Literal>
              </ogc:PropertyIsLessThan>
              <ogc:PropertyIsGreaterThanOrEqualTo>
                <ogc:PropertyName>pop_max</ogc:PropertyName>
                <ogc:Literal>100000</ogc:Literal>
              </ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:PropertyIsNotEqualTo>
                <ogc:PropertyName>is_capital</ogc:PropertyName>
                <ogc:Literal>true</ogc:Literal>
              </ogc:PropertyIsNotEqualTo>
            </ogc:And>
          </ogc:Filter>
          <sld:MaxScaleDenominator>4000000.0</sld:MaxScaleDenominator>
          <sld:PointSymbolizer>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>circle</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">#B5764B</sld:CssParameter>
                </sld:Fill>
                <sld:Stroke>
                  <sld:CssParameter name="stroke">#8C4A2F</sld:CssParameter>
                  <sld:CssParameter name="stroke-width">0.75</sld:CssParameter>
                </sld:Stroke>
              </sld:Mark>
              <sld:Size>6</sld:Size>
            </sld:Graphic>
          </sld:PointSymbolizer>
          <sld:TextSymbolizer>
            <sld:Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">Noto Sans</sld:CssParameter>
              <sld:CssParameter name="font-size">10</sld:CssParameter>
              <sld:CssParameter name="font-style">normal</sld:CssParameter>
              <sld:CssParameter name="font-weight">normal</sld:CssParameter>
            </sld:Font>
            <sld:LabelPlacement>
              <sld:PointPlacement>
                <sld:AnchorPoint>
                  <sld:AnchorPointX>0.5</sld:AnchorPointX>
                  <sld:AnchorPointY>0.0</sld:AnchorPointY>
                </sld:AnchorPoint>
                <sld:Displacement>
                  <sld:DisplacementX>0</sld:DisplacementX>
                  <sld:DisplacementY>5</sld:DisplacementY>
                </sld:Displacement>
              </sld:PointPlacement>
            </sld:LabelPlacement>
            <sld:Halo>
              <sld:Radius>1.2</sld:Radius>
              <sld:Fill>
                <sld:CssParameter name="fill">#F5EDE0</sld:CssParameter>
              </sld:Fill>
            </sld:Halo>
            <sld:Fill>
              <sld:CssParameter name="fill">#6B4226</sld:CssParameter>
            </sld:Fill>
            <sld:VendorOption name="autoWrap">60</sld:VendorOption>
          </sld:TextSymbolizer>
        </sld:Rule>
        <sld:Rule>
          <sld:Title>Small towns</sld:Title>
          <ogc:Filter>
            <ogc:And>
              <ogc:PropertyIsLessThan>
                <ogc:PropertyName>pop_max</ogc:PropertyName>
                <ogc:Literal>100000</ogc:Literal>
              </ogc:PropertyIsLessThan>
              <ogc:PropertyIsNotEqualTo>
                <ogc:PropertyName>is_capital</ogc:PropertyName>
                <ogc:Literal>true</ogc:Literal>
              </ogc:PropertyIsNotEqualTo>
            </ogc:And>
          </ogc:Filter>
          <sld:MaxScaleDenominator>600000.0</sld:MaxScaleDenominator>
          <sld:PointSymbolizer>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>circle</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">#C99B6A</sld:CssParameter>
                </sld:Fill>
                <sld:Stroke>
                  <sld:CssParameter name="stroke">#A6754A</sld:CssParameter>
                  <sld:CssParameter name="stroke-width">0.5</sld:CssParameter>
                </sld:Stroke>
              </sld:Mark>
              <sld:Size>4</sld:Size>
            </sld:Graphic>
          </sld:PointSymbolizer>
          <sld:TextSymbolizer>
            <sld:Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </sld:Label>
            <sld:Font>
              <sld:CssParameter name="font-family">Noto Sans</sld:CssParameter>
              <sld:CssParameter name="font-size">9</sld:CssParameter>
              <sld:CssParameter name="font-style">normal</sld:CssParameter>
              <sld:CssParameter name="font-weight">normal</sld:CssParameter>
            </sld:Font>
            <sld:LabelPlacement>
              <sld:PointPlacement>
                <sld:AnchorPoint>
                  <sld:AnchorPointX>0.5</sld:AnchorPointX>
                  <sld:AnchorPointY>0.0</sld:AnchorPointY>
                </sld:AnchorPoint>
                <sld:Displacement>
                  <sld:DisplacementX>0</sld:DisplacementX>
                  <sld:DisplacementY>4</sld:DisplacementY>
                </sld:Displacement>
              </sld:PointPlacement>
            </sld:LabelPlacement>
            <sld:Halo>
              <sld:Radius>1</sld:Radius>
              <sld:Fill>
                <sld:CssParameter name="fill">#F5EDE0</sld:CssParameter>
              </sld:Fill>
            </sld:Halo>
            <sld:Fill>
              <sld:CssParameter name="fill">#7A5A3A</sld:CssParameter>
            </sld:Fill>
            <sld:VendorOption name="autoWrap">60</sld:VendorOption>
          </sld:TextSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>

