JAR := xzit-starter/target/exam-back.jar
PROFILE ?= dev

.PHONY: help check clean build package run

help:
	@echo "可用命令:"
	@echo "  make check      - 检查 Maven 环境"
	@echo "  make clean      - 清理构建产物"
	@echo "  make build      - 清理并构建（跳过测试）"
	@echo "  make package    - 打包（跳过测试）"
	@echo "  make run        - 先 package，再 java -jar 启动（默认 PROFILE=dev）"

check:
	mvn -v

clean:
	mvn clean

package:
	mvn package -DskipTests

build: clean
	mvn package -DskipTests

run: package
	java -jar $(JAR) --spring.profiles.active=$(PROFILE)
