package ${packageName};

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Generated by Protogen
 * @since ${date}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ${className}ControllerMvcTest extends AbstractControllerMVCTests {
	
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void addShouldDisplayNew${className}Form() throws Exception {
       mockMvc.perform(get("${pathPrefix}/add${pathExtension}"))
       .andExpect(status().isOk())
       .andExpect(model().attributeExists("${className.substring(0, 1).toLowerCase()}${className.substring(1)}")) 
       .andExpect(view().name("${jspPath}/edit"));
    }
    
    @Test
    public void listShouldSimplyLoadPage() throws Exception {
       mockMvc.perform(get("${pathPrefix}/list${pathExtension}"))
       .andExpect(status().isOk())
       .andExpect(view().name("${jspPath}/list"));
    }
    
    @Test
    public void indexShouldDisplayListPage() throws Exception {
       mockMvc.perform(get("${pathPrefix}/"))
       .andExpect(status().isOk())
       .andExpect(view().name("${jspPath}/list"));
    }
    
    @Test
    public void listAltShouldLoadListOf${className}s() throws Exception {
       mockMvc.perform(get("${pathPrefix}/list_alt${pathExtension}"))
       .andExpect(status().isOk())
       .andExpect(model().attributeExists("${className.substring(0, 1).toLowerCase()}${className.substring(1)}List")) 
       .andExpect(view().name("${jspPath}/list_alt"));
    }
}