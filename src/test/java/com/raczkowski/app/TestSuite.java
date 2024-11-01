package com.raczkowski.app;

import com.raczkowski.app.article.ArticleServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ArticleServiceTest.class,
})
public class TestSuite {
}
