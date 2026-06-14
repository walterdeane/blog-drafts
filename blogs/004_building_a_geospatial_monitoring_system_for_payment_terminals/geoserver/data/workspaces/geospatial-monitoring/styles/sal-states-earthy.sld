<?xml version="1.0" encoding="UTF-8"?><sld:StyledLayerDescriptor xmlns:sld="http://www.opengis.net/sld" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns="http://www.opengis.net/sld" version="1.0.0">
  <sld:NamedLayer>
    <sld:Name>Default Styler</sld:Name>
    <sld:UserStyle>
      <sld:Name>Default Styler</sld:Name>
      <sld:Title>SAL areas coloured by state, earthy palette</sld:Title>
      <sld:FeatureTypeStyle>
        <sld:Name>name</sld:Name>
        <sld:Rule>
          <sld:Title>State fill</sld:Title>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">
                <ogc:Function name="Recode">
                  <ogc:PropertyName>ste_code21</ogc:PropertyName>
                  <ogc:Literal>1</ogc:Literal>
                  <ogc:Literal>#D9A66C</ogc:Literal>
                  <ogc:Literal>2</ogc:Literal>
                  <ogc:Literal>#C97B63</ogc:Literal>
                  <ogc:Literal>3</ogc:Literal>
                  <ogc:Literal>#E0C087</ogc:Literal>
                  <ogc:Literal>4</ogc:Literal>
                  <ogc:Literal>#B98B73</ogc:Literal>
                  <ogc:Literal>5</ogc:Literal>
                  <ogc:Literal>#CDB07A</ogc:Literal>
                  <ogc:Literal>6</ogc:Literal>
                  <ogc:Literal>#A6886B</ogc:Literal>
                  <ogc:Literal>7</ogc:Literal>
                  <ogc:Literal>#D8B384</ogc:Literal>
                  <ogc:Literal>8</ogc:Literal>
                  <ogc:Literal>#C2A37E</ogc:Literal>
                  <ogc:Literal>9</ogc:Literal>
                  <ogc:Literal>#BFAE96</ogc:Literal>
                  <ogc:Literal>Z</ogc:Literal>
                  <ogc:Literal>#BFAE96</ogc:Literal>
                </ogc:Function>
              </sld:CssParameter>
            </sld:Fill>
          </sld:PolygonSymbolizer>
        </sld:Rule>
        <sld:Rule>
          <sld:Title>Suburb outlines</sld:Title>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#8C7A66</sld:CssParameter>
              <sld:CssParameter name="stroke-opacity">0.6</sld:CssParameter>
              <sld:CssParameter name="stroke-width">0.3</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>

