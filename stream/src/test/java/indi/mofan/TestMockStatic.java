package indi.mofan;

import indi.mofan.domain.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author mofan
 * @date 2022/3/3 21:07
 */
public class TestMockStatic {
    @Test
    public void testNoParamStaticMethod() {
        MockedStatic<TestUtil> mockStatic = Mockito.mockStatic(TestUtil.class);
        mockStatic.when(TestUtil::helloWorld).thenReturn("Hello, Mofan");
        Assert.assertEquals(TestUtil.helloWorld(), "Hello, Mofan");
        
        // mock 后，未进行 stubbing 的方法返回 null
        Assert.assertNull(TestUtil.generateHello("boy"));
        // 注销注册的静态 Mock
        mockStatic.close();
    }

    @Test
    public void testHaveParamStaticMethod() {
        MockedStatic<TestUtil> mockStatic = Mockito.mockStatic(TestUtil.class);
        mockStatic.when(() -> TestUtil.generateHello("boy")).thenReturn("Hello, girl");
        Assert.assertEquals(TestUtil.generateHello("boy"), "Hello, girl");
        // 进行 verify
        mockStatic.verify(() -> TestUtil.generateHello(Mockito.anyString()), Mockito.times(1));
        mockStatic.close();
    }
    
    @Test
    public void testTryWithResources() {
        Assert.assertEquals(TestUtil.generateHello("boy"), "Hello, boy");
        try (MockedStatic<TestUtil> mockStatic = Mockito.mockStatic(TestUtil.class)) {
            mockStatic.when(() -> TestUtil.generateHello("boy")).thenReturn("Hello, girl");
            Assert.assertEquals(TestUtil.generateHello("boy"), "Hello, girl");
        }
        Assert.assertEquals(TestUtil.generateHello("boy"), "Hello, boy");
    }
}
