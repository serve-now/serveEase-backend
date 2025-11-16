# Serve-Now 

매장 운영자가 **테이블 → 주문 → 결제 → 매출 리포트**까지 한 번에 관리할 수 있도록 돕는 프로그램 입니다.<br>
점주 대시보드, 매장 직원 단말, 고객 스캐너 등 다양한 채널에서 일관된 API를 제공합니다.<br>

### 🌐 [Serve-Now](https://serve-now.site/)  클릭 (바로가기) <br>
> 🚩 테스트 계정으로 로그인 후 사용 가능합니다 <br>
> - ID : wjdwwidz<br>
> - PW : qwer1234!!


## 기술 스택
### Backend
|  | 스택                                                                         |
| --- |----------------------------------------------------------------------------|
| Language | `Java 17`                                                                  |
| Framework | `Spring Boot 3.5`, `Spring Web`, `Spring Data JPA`, `Spring Security`      |
| DB | `MariaDB (Amazon RDS)`                                                     |
| Infra | `RabbitMQ`, `Docker`, `AWS EC2`, `NGINX Reverse Proxy` |



## API 명세
Swagger 를 사용한 명세<br>
> 🌐 https://api.servenow.site/swagger-ui/index.html

## ERD
- DataGrip
<img width="5196" height="1440" alt="serveasedb@rds amazonaws" src="https://github.com/user-attachments/assets/91992545-8d72-4352-a32f-ec8eff752841" />

## 인프라 구조
<div align="center">
  <img src="https://github-production-user-asset-6210df.s3.amazonaws.com/88189246/514915791-755ca709-568e-4a62-a752-ced838d3bfee.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20251116%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251116T200751Z&X-Amz-Expires=300&X-Amz-Signature=7f8b661eb769aa007932d052f6b2cd215a67234efcc53ebf78bcae4415b950a0&X-Amz-SignedHeaders=host" />
</div>



## 상세 설명
- [기획 문서](https://colossal-paste-49e.notion.site/268637c8b42481519a96f647c89e2b85?source=copy_link) (Notion) 살펴보기 📕 


### 로그인 및 메인
<div align="center">
  <img src="https://github.com/user-attachments/assets/b9bee115-2d1e-41e7-bb9b-0a4f874fa85e" width="49%" />
  <img src="https://github.com/user-attachments/assets/186e11c0-9886-4565-b94a-cf44d4691022" width="49%" />
</div>


### 주문

<div align="center">
  <img src="https://github.com/user-attachments/assets/e0932b34-5c8e-4a59-a335-4501ded6118a" width="49%"/>
  <img src="https://github.com/user-attachments/assets/d51bbbf5-df37-4d8e-86bb-d18c15fa370e" width="49%" />
</div>






### 상품 관리

<div align="center">
  <img src="https://github.com/user-attachments/assets/e292c574-1517-4351-8e35-750f536440ec" width="49%" />
  <img src="https://github.com/user-attachments/assets/816b6272-e9f8-46b8-8818-45151c8ff6bc" width="49%" />
</div>



### 결제 
<div align="center">
  <img src="https://github.com/user-attachments/assets/f3b77848-9480-4bc4-80df-77fc86e98a00" width="49%" />
  <img src="https://github.com/user-attachments/assets/1bea8ddd-d226-4585-823a-4f3cf48a0e99" width="49%" />
</div>




### 분할 결제 
<div align="center">
  <img src="https://github.com/user-attachments/assets/d05fdf1e-40b5-4c8f-af94-d7f71fffe75e" width="49%" />
  <img src="https://github.com/user-attachments/assets/b4fea24d-dc10-403d-8765-1e3df0d0910e" width="49%" />
</div>



### 매출
매출 현황 (차트)
<div align="center">
  <img src="https://github.com/user-attachments/assets/3179b6d2-3d05-4035-bb53-c17c84026bc7" width="49%" />
  <img src="https://github.com/user-attachments/assets/b7e4e1eb-cb1c-4446-97d2-68e0e5906e4d" width="49%" />
</div>



### 결제 내역 상세
<div align="center">
  <img src="https://github.com/user-attachments/assets/f9f3aae8-33b2-47ce-9926-0286f8aa772d" width="49%" />
  <img src="https://github.com/user-attachments/assets/e36c08af-0775-4454-ba5b-5869bf028cd2" width="49%" />
</div>





