# Serve-Now 🍽️
매장에서 주문과 결제를 처리하는 POS 시스템입니다.<br>
실시간 주문 상태, 매출 집계, 결제 취소 등 운영에 필요한 통합 관리를 제공합니다.


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
<img width="1100" height="596" alt="스크린샷 2025-11-17 오후 3 39 49" src="https://github.com/user-attachments/assets/979a8cfd-25e6-46ef-b8b7-c338f303780f" />




## 상세 설명
- [기획 문서](https://colossal-paste-49e.notion.site/268637c8b42481519a96f647c89e2b85?source=copy_link) (Notion) 살펴보기 📕 


### <로그인 및 메인>
- 점주 계정을 인증해 성공시 JWT를 발급합니다.
- 회원가입시 테이블을 생성하여 별도의 설정 없이 즉시 포스 프로그램 사용이 가능합니다.
<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/b9bee115-2d1e-41e7-bb9b-0a4f874fa85e"
    style="width: 49%; height: 240px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/186e11c0-9886-4565-b94a-cf44d4691022"
    style="width: 49%; height: 240px; object-fit: cover;" 
  />
</div>
<br>


### <주문>
- 테이블을 클릭하여 주문 생성 및 추가 주문이 가능합니다.
- 테이블별로 진행 상태(`접수`/`조리`/`완료`)를 표시해 직원 간 커뮤니케이션 비용을 절감합니다.
<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/a8c78425-bf4b-4d2b-adee-80dd0a14a6cd"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/dd335fd4-5c4b-41ac-8c80-cafd27c65c37" 
    style="width: 49%; height: 300px; object-fit: cover;" />
</div>
<br>





### <상품 관리>
- 상품 관리 화면에서 메뉴의 이름, 설명, 가격, 노출 여부를 설정할 수 있습니다.
- 카테고리 설정 탭을 눌러 카테고리를 관리할 수 있습니다.
<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/e292c574-1517-4351-8e35-750f536440ec"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/816b6272-e9f8-46b8-8818-45151c8ff6bc"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
</div>
<br>



### <결제> 
- `단일 결제`와 `분할 결제`를 지원합니다.
- 각 결제 시도마다 `현금`과 `토스페이먼츠 연동 간편결제`를 지원하며 현장 상황에 맞춰 수단을 선택할 수 있습니다.
- 결제가 승인되는 즉시 영수증 데이터가 매출 집계 데이터로 이어져 이후 정산 단계에서 동일한 데이터를 참조합니다.
#### 단일 결제 화면
<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/f3b77848-9480-4bc4-80df-77fc86e98a00"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/1bea8ddd-d226-4585-823a-4f3cf48a0e99"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
</div>

#### 분할 결제 화면
<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/d05fdf1e-40b5-4c8f-af94-d7f71fffe75e"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/b4fea24d-dc10-403d-8765-1e3df0d0910e"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
</div>
<br>



### <매출>
#### 매출 차트 
- 실시간 결제 데이터를 집계해 대시보드 형태로 보여줍니다.
- `일/주/월` 단위를 선택해 매출 추이를 차트로 확인할 수 있습니다.
- 실매출, 주문 건당 평균가, 취소 금액이 공통으로 표시됩니다.
- 결제/취소 이벤트가 발생할 때마다 자동으로 동기화 되어 별도의 리로드 없이 실시간으로 활용할 수 있습니다.

#### 매출 달력
- 실시간 결제 데이터를 집계해 달력 형태로 보여줍니다.
- 현재 월 총액, 주차별 합계가 표시됩니다.

<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/a1565a73-d8d4-45bb-843d-f6adf7ba3893"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/b7e4e1eb-cb1c-4446-97d2-68e0e5906e4d"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
</div><br>




### < 결제 내역 상세 >
- 결제 목록에서 내역 하나를 선택하면 상세 정보를 조회할 수 있습니다.
- 목록에서 결제는 `부분결제`, `부분 취소`, `완료`, `취소` 상태 중 하나로 보여집니다.
- 필터링 기능으로 빠른 검색을 제공합니다.
<div align="center" style="display: flex; gap: 6px;">
  <img 
    src="https://github.com/user-attachments/assets/f9f3aae8-33b2-47ce-9926-0286f8aa772d"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
  <img 
    src="https://github.com/user-attachments/assets/e36c08af-0775-4454-ba5b-5869bf028cd2"
    style="width: 49%; height: 300px; object-fit: cover;" 
  />
</div><br>

### <결제 취소>
- 결제 상세 화면에서 취소 버튼으로 결제를 빠르게 진행할 수 있습니다.
- 분할 결제인 경우에는 결제 건마다 취소 버튼이 생성됩니다.
- 취소된 결제건은 실 결제액을 되돌린 후 `취소` 상태로 표시되며 매출/정산 지표가 즉시 조정됩니다.

<div align="center" style="display: flex; gap: 6px;">
  <img width="1770" height="886" alt="image" src="https://github.com/user-attachments/assets/623b33c3-c0ce-4408-a504-7d771dd65483" style="width: 49%; height: 300px; object-fit: cover;" />
  <img width="1526" height="925" alt="image" src="https://github.com/user-attachments/assets/3973fa52-554f-441f-8e0e-bd5543f38835" style="width: 49%; height: 300px; object-fit: cover;" />
</div><br>






