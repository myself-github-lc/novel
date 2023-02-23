package com.myself.novel.collect;

import com.myself.novel.constant.CollectorSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NovelCollectorManager implements BeanFactoryAware {

    private Map<CollectorSource, NovelCollector> sourceCollector;

    public NovelCollector getCollector(CollectorSource collectorSource){
        return sourceCollector.get(collectorSource);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        sourceCollector = defaultListableBeanFactory.getBeansOfType(NovelCollector.class)
                .values()
                .stream()
                .collect(Collectors.toMap(NovelCollector::getSource, Function.identity()));
    }
}