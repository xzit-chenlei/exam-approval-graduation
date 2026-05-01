.PHONY: help check clean build package

help:
	@echo "可用命令:"
	@echo "  make check      - 检查 Maven 环境"
	@echo "  make clean      - 清理构建产物"
	@echo "  make build      - 清理并构建（跳过测试）"
	@echo "  make package    - 打包（跳过测试）"

check:
	mvn -v

clean:
	mvn clean

package:
	mvn package -DskipTests

build: clean
	mvn package -DskipTests
